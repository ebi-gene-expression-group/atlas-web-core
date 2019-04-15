package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.EmbeddedSolrCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.AnalyticsCollectionProxy;
import uk.ac.ebi.atlas.testutils.JdbcUtils;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnalyticsIndexerServiceIT {
    @Inject
    DataSource dataSource;

    @Inject
    JdbcUtils jdbcUtils;

    // TODO This test will be feasible when we move the experiment traders to atlas-web-core
    // @Inject
    // ExperimentTrader experimentTrader;

    @Inject
    private EmbeddedSolrCollectionProxyFactory embeddedSolrCollectionProxyFactory;

    @Inject
    private ExperimentDataPointStreamFactory experimentDataPointStreamFactory;

    @Mock
    private SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactoryMock;

    private AnalyticsIndexerService subject;

    @BeforeAll
    void populateDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/experiment-fixture.sql"));
        populator.execute(dataSource);
    }

    @AfterAll
    void cleanDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
        populator.execute(dataSource);
    }

    @BeforeEach
    void setUp() {
        when(solrCloudCollectionProxyFactoryMock.create(AnalyticsCollectionProxy.class))
                .thenReturn(embeddedSolrCollectionProxyFactory.createAnalyticsCollectionProxy());

        subject = new AnalyticsIndexerService(solrCloudCollectionProxyFactoryMock, experimentDataPointStreamFactory);
    }

    @Ignore
    @ParameterizedTest
    @MethodSource("experimentAccessionProvider")
    void indexExperiment() {

    }

    private Stream<String> experimentAccessionProvider() {
        return Stream.of(jdbcUtils.fetchRandomPublicExpressionAtlasExperimentAccession());
    }


}