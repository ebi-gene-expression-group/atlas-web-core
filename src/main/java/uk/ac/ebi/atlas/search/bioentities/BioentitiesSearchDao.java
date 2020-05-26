package uk.ac.ebi.atlas.search.bioentities;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Service;
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

@Service
public class BioentitiesSearchDao {
    private final BioentitiesCollectionProxy bioentitiesCollectionProxy;
    
    public BioentitiesSearchDao(SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        bioentitiesCollectionProxy = solrCloudCollectionProxyFactory.create(BioentitiesCollectionProxy.class);
    }

    public ImmutableSet<String> parseStringFieldFromMatchingDocs(SemanticQuery geneSemanticQuery,
                                                                 SchemaField<BioentitiesCollectionProxy> field) {
        // The fact that this edge case can’t be handled by the code below is a bit inelegant, but it’s either this or
        // adding a clause to MergeStreamBuilder so that an empty collection of tuple streams return an empty tuple
        // stream, which might mask potential mis-use of merge. Also, an empty semantic query is alright, although we
        // won’t have this case often.
        if (geneSemanticQuery.isEmpty()) {
            return ImmutableSet.of();
        }

        var searchStreamBuilders =
                geneSemanticQuery.terms().stream()
                        .map(semanticQueryTerm -> createSolrQueryBuilder(semanticQueryTerm, field))
                        .map(solrQueryBuilder ->
                                new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder)
                                        .returnAllDocs())
                        .collect(toImmutableSet());

        return parseTupleStreamStringField(new MergeStreamBuilder(searchStreamBuilders, field.name()), field.name());
    }

    public ImmutableSet<String> parseStringFieldFromMatchingDocs(SemanticQuery geneSemanticQuery,
                                                                 Species species,
                                                                 SchemaField<BioentitiesCollectionProxy> field) {
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

        return parseTupleStreamStringField(
                new MergeStreamBuilder(searchStreamBuilders, field.name()),
                field.name());
    }

    public ImmutableSet<String> parseStringFieldFromMatchingDocs(SemanticQueryTerm geneSemanticQueryTerm,
                                                                 SchemaField<BioentitiesCollectionProxy> field) {
        var solrQueryBuilder = createSolrQueryBuilder(geneSemanticQueryTerm, field);
        return parseTupleStreamStringField(
                new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).returnAllDocs(),
                field.name());
    }

    public ImmutableSet<String> parseStringFieldFromMatchingDocs(SemanticQueryTerm geneSemanticQueryTerm,
                                                                 Species species,
                                                                 SchemaField<BioentitiesCollectionProxy> field) {
        var solrQueryBuilder =
                createSolrQueryBuilder(geneSemanticQueryTerm, field)
                        .addFilterFieldByTerm(SPECIES, species.getEnsemblName());
        return parseTupleStreamStringField(
                new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).returnAllDocs(),
                field.name());
    }

    // Create a SolrQueryBuilder for a given SemanticQueryTerm that returns only the sorting field
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

    // Parse a string field name from a stream of tuples (i.e. Solr docs) in the same order as they are received
    private static ImmutableSet<String> parseTupleStreamStringField(TupleStreamBuilder tupleStreamBuilder, 
                                                                    String fieldName) {
        try (var tupleStreamer = TupleStreamer.of(tupleStreamBuilder.build())) {
            return tupleStreamer.get()
                    .map(tuple -> tuple.getString(fieldName))
                    .collect(toImmutableSet());
        }
    }
}
