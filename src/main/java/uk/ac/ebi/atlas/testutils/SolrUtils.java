package uk.ac.ebi.atlas.testutils;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;

import static org.apache.solr.client.solrj.SolrQuery.ORDER.asc;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.AnalyticsSchemaField;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.IS_PRIVATE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.KEYWORD_SYMBOL;

import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source.SearchStreamBuilder;

@Component
public class SolrUtils {
    private final BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;
    private final BioentitiesCollectionProxy bioentitiesCollectionProxy;

    public SolrUtils(SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        bulkAnalyticsCollectionProxy = solrCloudCollectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
        bioentitiesCollectionProxy = solrCloudCollectionProxyFactory.create(BioentitiesCollectionProxy.class);
    }

    public String fetchRandomGeneIdFromAnalytics() {
        var searchStreamBuilder = new SearchStreamBuilder<>(
                bulkAnalyticsCollectionProxy,
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addFilterFieldByTerm(IS_PRIVATE, "false")
                        .setFieldList(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER)
                        .sortBy(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER, asc))
                .returnAllDocs();

        try (var tupleStreamer = TupleStreamer.of(searchStreamBuilder.build()).get()) {
            return tupleStreamer
                    .findAny()
                    .orElseThrow()
                    .getString(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER.name());
        }
    }

    public String fetchRandomGeneIdFromAnalytics(String experimentAccession) {
        var searchStreamBuilder = new SearchStreamBuilder<>(
                bulkAnalyticsCollectionProxy,
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addFilterFieldByTerm(EXPERIMENT_ACCESSION, experimentAccession)
                        .addFilterFieldByTerm(IS_PRIVATE, "false")
                        .setFieldList(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER)
                        .sortBy(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER, asc))
                .returnAllDocs();

        try (var tupleStreamer = TupleStreamer.of(searchStreamBuilder.build()).get()) {
            return tupleStreamer
                    .findAny()
                    .orElseThrow()
                    .getString(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER.name());
        }
    }

    public String fetchRandomGeneIdFromAnalytics(AnalyticsSchemaField field, String term) {
        var searchStreamBuilder = new SearchStreamBuilder<>(
                bulkAnalyticsCollectionProxy,
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addQueryFieldByTerm(field, term)
                        .addFilterFieldByTerm(IS_PRIVATE, "false")
                        .setFieldList(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER)
                        .sortBy(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER, asc))
                .returnAllDocs();

        try (var tupleStreamer = TupleStreamer.of(searchStreamBuilder.build()).get()) {
            return tupleStreamer
                    .findAny()
                    .orElseThrow()
                    .getString(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER.name());
        }
    }

    public String fetchRandomGeneWithoutSymbolFromAnalytics() {
        var searchStreamBuilder = new SearchStreamBuilder<>(
                bulkAnalyticsCollectionProxy,
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        // We need at least one query field for the search expression to work (!)
                        .addQueryFieldByTerm(KEYWORD_SYMBOL, "")
                        .addQueryFieldByTerm(IS_PRIVATE, "false")
                        .setFieldList(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER)
                        .sortBy(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER, asc))
                .returnAllDocs();

        try (var tupleStreamer = TupleStreamer.of(searchStreamBuilder.build()).get()) {
            return tupleStreamer
                    .findAny()
                    .orElseThrow()
                    .getString(BIOENTITY_IDENTIFIER.name());
        }
    }

    public String fetchRandomGeneOfSpecies(String species) {
        var searchStreamBuilder = new SearchStreamBuilder<>(
                bioentitiesCollectionProxy,
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addFilterFieldByTerm(SPECIES, species)
                        .setFieldList(BIOENTITY_IDENTIFIER)
                        .sortBy(BIOENTITY_IDENTIFIER, asc))
                .returnAllDocs();

        try (var tupleStreamer = TupleStreamer.of(searchStreamBuilder.build()).get()) {
            return tupleStreamer
                    .findAny()
                    .orElseThrow()
                    .getString(BIOENTITY_IDENTIFIER.name());
        }
    }
}
