package uk.ac.ebi.atlas.search.bioentities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;

import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator.IntersectStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator.MergeStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator.SelectStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source.SearchStreamBuilder;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.solr.client.solrj.SolrQuery.ORDER.asc;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.toDocValues;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;

@Component
public class BioentitiesSearchDao {
    private final BioentitiesCollectionProxy bioentitiesCollectionProxy;
    // TODO Replace with gxa-gene2experiment collection when it’s added to our SolrCloud cluster
    private final BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;
    
    public BioentitiesSearchDao(SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        bioentitiesCollectionProxy = solrCloudCollectionProxyFactory.create(BioentitiesCollectionProxy.class);
        bulkAnalyticsCollectionProxy = solrCloudCollectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
    }

    public ImmutableSet<String> searchGeneIds(String experimentAccession, SemanticQuery geneSemanticQuery) {
        // Without this, the merge stream would throw because it would be built without any streams
        if (geneSemanticQuery.isEmpty()) {
            return ImmutableSet.of();
        }

        var matchingGeneIdsAcrossAllExperiments =
                createTupleStreamBuilder(geneSemanticQuery, BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER);

        var experimentGeneIds =
                new SearchStreamBuilder<>(
                        bulkAnalyticsCollectionProxy,
                        new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                                .addFilterFieldByTerm(EXPERIMENT_ACCESSION, experimentAccession)
                                .setFieldList(BIOENTITY_IDENTIFIER)
                                .sortBy(BIOENTITY_IDENTIFIER, asc))
                        .returnAllDocs();

        return parseStringField(
                new IntersectStreamBuilder(matchingGeneIdsAcrossAllExperiments, experimentGeneIds, BIOENTITY_IDENTIFIER.name()),
                BIOENTITY_IDENTIFIER.name());
    }

    public ImmutableSet<String> searchSpecies(SemanticQuery geneSemanticQuery) {
        // Without this, the merge stream would throw because it would be built without any streams
        if (geneSemanticQuery.isEmpty()) {
            return ImmutableSet.of();
        }

        return parseStringField(createTupleStreamBuilder(geneSemanticQuery, SPECIES), SPECIES.name());
    }

    // Maps a SemanticQuery to a streaming expression that returns tuples (that satisfy the query) with a single field
    private SelectStreamBuilder createTupleStreamBuilder(SemanticQuery geneSemanticQuery,
                                                         SchemaField<BioentitiesCollectionProxy> field) {
        return new SelectStreamBuilder(
                        // Map query terms to search streams and merge them
                        new MergeStreamBuilder(
                                geneSemanticQuery.terms().stream()
                                        .map(semanticQueryTerm ->
                                                new SearchStreamBuilder<>(
                                                        bioentitiesCollectionProxy,
                                                        // returnAllDocs() requires DocValues field
                                                        createSolrQueryBuilder(semanticQueryTerm, toDocValues(field)))
                                                        .returnAllDocs())
                                        .collect(toImmutableSet()),
                                toDocValues(field).name()))
                        // Rename the DocValues field for the intersection below
                        .addFieldMapping(ImmutableMap.of(toDocValues(field).name(), field.name()));
    }

    // Map a SemanticQueryTerm to one-field SolrQueryBuilder over BioentitiesCollectionProxy
    private static SolrQueryBuilder<BioentitiesCollectionProxy> createSolrQueryBuilder(
            SemanticQueryTerm geneSemanticQueryTerm,
            SchemaField<BioentitiesCollectionProxy> field) {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(PROPERTY_VALUE, geneSemanticQueryTerm.value())
                        .setFieldList(field)
                        .sortBy(field, asc);

        geneSemanticQueryTerm.category()
                .ifPresent(category -> solrQueryBuilder.addQueryFieldByTerm(PROPERTY_NAME, category));

        return solrQueryBuilder;
    }

    // Parse a string field from a stream of tuples (i.e. Solr docs) in the same order as they are received
    private static ImmutableSet<String> parseStringField(TupleStreamBuilder tupleStreamBuilder,
                                                         String fieldName) {
        try (var tupleStreamer = TupleStreamer.of(tupleStreamBuilder.build())) {
            return tupleStreamer.get()
                    .map(tuple -> tuple.getString(fieldName))
                    .collect(toImmutableSet());
        }
    }
}
