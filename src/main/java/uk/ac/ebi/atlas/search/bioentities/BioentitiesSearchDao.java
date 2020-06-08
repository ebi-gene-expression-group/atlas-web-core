package uk.ac.ebi.atlas.search.bioentities;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;

import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator.MergeStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source.SearchStreamBuilder;
import uk.ac.ebi.atlas.species.Species;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.solr.client.solrj.SolrQuery.ORDER.asc;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;

@Component
public class BioentitiesSearchDao {
    private final BioentitiesCollectionProxy bioentitiesCollectionProxy;
    
    public BioentitiesSearchDao(SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        bioentitiesCollectionProxy = solrCloudCollectionProxyFactory.create(BioentitiesCollectionProxy.class);
    }

    public ImmutableSet<String> parseStringFieldFromMatchingDocs(SemanticQuery geneSemanticQuery,
                                                                 SchemaField<BioentitiesCollectionProxy> field) {
        var searchStreamBuilders =
                geneSemanticQuery.terms().stream()
                        .map(semanticQueryTerm -> createSolrQueryBuilder(semanticQueryTerm, field))
                        .map(solrQueryBuilder ->
                                new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder)
                                        .returnAllDocs())
                        .collect(toImmutableSet());

        return mergeStreamsAndParseTupleStringField(searchStreamBuilders, field.name());
    }

    public ImmutableSet<String> parseStringFieldFromMatchingDocs(SemanticQuery geneSemanticQuery,
                                                                  Species species,
                                                                  SchemaField<BioentitiesCollectionProxy> field) {
        // The same as above but we filter by species
        var searchStreamBuilders =
                geneSemanticQuery.terms().stream()
                        .map(semanticQueryTerm ->
                                createSolrQueryBuilder(semanticQueryTerm, field))
                        .map(solrQueryBuilder ->
                                solrQueryBuilder.addFilterFieldByTerm(SPECIES, species.getEnsemblName()))
                        .map(solrQueryBuilder ->
                                new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder)
                                        .returnAllDocs())
                        .collect(toImmutableSet());

        return mergeStreamsAndParseTupleStringField(searchStreamBuilders, field.name());
    }

    // Parse a string field from a stream of tuples (i.e. Solr docs) in the same order as they are received
    private ImmutableSet<String> mergeStreamsAndParseTupleStringField(
            ImmutableCollection<? extends TupleStreamBuilder> tupleStreamBuilders,
            String fieldName) {
        // Warning: an empty collection of tuple stream builders might mask potential misuses of merge
        if (tupleStreamBuilders.isEmpty()) {
            return ImmutableSet.of();
        }

        try (var tupleStreamer = TupleStreamer.of(new MergeStreamBuilder(tupleStreamBuilders, fieldName).build())) {
            return tupleStreamer.get()
                    .map(tuple -> tuple.getString(fieldName))
                    .collect(toImmutableSet());
        }
    }

    // Create a SolrQueryBuilder for a given SemanticQueryTerm that returns only a single (naturally sorted) field
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
}
