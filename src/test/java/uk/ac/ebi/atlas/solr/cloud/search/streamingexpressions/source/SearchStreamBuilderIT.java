package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import javax.inject.Inject;

import java.io.UncheckedIOException;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.stream.Collectors.toList;
import static org.apache.solr.client.solrj.SolrQuery.ORDER.asc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.FACTOR_VALUE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SearchStreamBuilderIT {
    private static final int MAX_NUM_ROWS = 100;

    @Inject
    private SolrCloudCollectionProxyFactory collectionProxyFactory;

    private BioentitiesCollectionProxy bioentitiesCollectionProxy;
    private SingleCellAnalyticsCollectionProxy singleCellAnalyticsCollectionProxy;

    @BeforeEach
    void setUp() {
        bioentitiesCollectionProxy = collectionProxyFactory.create(BioentitiesCollectionProxy.class);
        singleCellAnalyticsCollectionProxy = collectionProxyFactory.create(SingleCellAnalyticsCollectionProxy.class);
    }

    @Test
    void testMinimalQuery() {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .setFieldList(ImmutableSet.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, asc);

        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList())).isNotEmpty();
        }
    }

    @Test
    void testQuery() {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(SPECIES, "Mus_musculus")
                        .setFieldList(ImmutableSet.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, asc);

        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList()))
                    .allSatisfy(
                            tuple -> assertThat(tuple.getString(BIOENTITY_IDENTIFIER.name())).startsWith("ENSMUSG"));
        }
    }

    @Test
    void noResults() {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(PROPERTY_VALUE, "Foobar")
                        .setFieldList(ImmutableSet.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, asc);

        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList()))
                    .isEmpty();
        }
    }

    @Test
    void requiresSortFieldAndToBePresentInFieldList() {
        var solrQueryBuilder = new SolrQueryBuilder<BioentitiesCollectionProxy>();

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build());

        solrQueryBuilder.sortBy(BIOENTITY_IDENTIFIER, asc);
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build());

        solrQueryBuilder.setFieldList(BIOENTITY_IDENTIFIER);
        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList()))
                    .isNotEmpty();
        }
    }

    @Test
    void canReturnAllRowsWihtoutSetting() {
        var numRows = ThreadLocalRandom.current().nextInt(MAX_NUM_ROWS);
        var solrQueryBuilder =
                new SolrQueryBuilder<SingleCellAnalyticsCollectionProxy>()
                        .addQueryFieldByTerm(EXPERIMENT_ACCESSION, "E-EHCA-2")
                        .setFieldList(EXPERIMENT_ACCESSION)
                        .setRows(numRows)
                        .sortBy(EXPERIMENT_ACCESSION, asc);

        var searchStreamBuilder = new SearchStreamBuilder<>(singleCellAnalyticsCollectionProxy, solrQueryBuilder);
        try (var tupleStreamer = TupleStreamer.of(searchStreamBuilder.build())) {
            assertThat(tupleStreamer.get().count()).isEqualTo(numRows);
        }

        var allDocsSearchStreamBuilder =
                new SearchStreamBuilder<>(singleCellAnalyticsCollectionProxy, solrQueryBuilder).returnAllDocs();
        try (var tupleStreamer = TupleStreamer.of(allDocsSearchStreamBuilder.build())) {
            assertThat(tupleStreamer.get().count()).isGreaterThan(numRows);
        }
    }

    @Test
    void throwsIfFieldListContainsMultivaluedFieldWithoutDocValuesWhenReturningAllFields() {
        var numRows = ThreadLocalRandom.current().nextInt(MAX_NUM_ROWS);
        var solrQueryBuilder =
                new SolrQueryBuilder<SingleCellAnalyticsCollectionProxy>()
                        .addQueryFieldByTerm(EXPERIMENT_ACCESSION, "E-EHCA-2")
                        .setFieldList(ImmutableSet.of(EXPERIMENT_ACCESSION, FACTOR_VALUE))
                        .setRows(numRows)
                        .sortBy(EXPERIMENT_ACCESSION, asc);

        var searchStreamBuilder = new SearchStreamBuilder<>(singleCellAnalyticsCollectionProxy, solrQueryBuilder);
        // No exceptions down here...
        TupleStreamer.of(searchStreamBuilder.build());

        var allDocsSearchStreamBuilder =
                new SearchStreamBuilder<>(singleCellAnalyticsCollectionProxy, solrQueryBuilder).returnAllDocs();
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> TupleStreamer.of(allDocsSearchStreamBuilder.build()));
    }
}
