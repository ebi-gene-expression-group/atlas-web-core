package uk.ac.ebi.atlas.experimentimport.idf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class IdfParserIT {
    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = TestConfig.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Bulk {
        @Inject
        private DataSource dataSource;

        @Inject
        private Path dataFilesPath;

        @Inject
        private JdbcUtils jdbcUtils;

        @BeforeAll
        void populateDatabaseTables() {
            ResourceDatabasePopulator bulkPopulator = new ResourceDatabasePopulator();
            bulkPopulator.addScripts(new ClassPathResource("fixtures/gxa-experiment-fixture.sql"));
            bulkPopulator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
            populator.execute(dataSource);
            populator.execute(dataSource);
        }

        @ParameterizedTest
        @MethodSource("expressionAtlasExperimentsProvider")
        void testParserForExpressionAtlas(String experimentAccession) {
            IdfParser idfParser = new IdfParser(new DataFileHub(dataFilesPath.resolve("gxa")));

            IdfParserOutput result = idfParser.parse(experimentAccession);

            assertThat(result.getExpectedClusters()).isEqualTo(0);
            assertThat(result.getTitle()).isNotEmpty();
            assertThat(result.getExperimentDescription()).isNotEmpty();
            assertThat(result.getPublications()).isNotNull();
        }

        private Iterable<String> expressionAtlasExperimentsProvider() {
            return jdbcUtils.fetchAllExperimentAccessions();
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = TestConfig.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SingleCell {
        @Inject
        private DataSource dataSource;

        @Inject
        private Path dataFilesPath;

        @Inject
        private JdbcUtils jdbcUtils;

        @BeforeAll
        void populateDatabaseTables() {
            ResourceDatabasePopulator bulkPopulator = new ResourceDatabasePopulator();
            bulkPopulator.addScripts(new ClassPathResource("fixtures/scxa-experiment-fixture.sql"));
            bulkPopulator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
            populator.execute(dataSource);
            populator.execute(dataSource);
        }

        @ParameterizedTest
        @MethodSource("singleCellExperimentsProvider")
        void testParserForSingleCell(String experimentAccession) {
            IdfParser idfParser = new IdfParser(new DataFileHub(dataFilesPath.resolve("scxa")));
            IdfParserOutput result = idfParser.parse(experimentAccession);

            assertThat(result.getExpectedClusters()).isGreaterThanOrEqualTo(0);
            assertThat(result.getTitle()).isNotEmpty();
            assertThat(result.getExperimentDescription()).isNotEmpty();
            assertThat(result.getPublications()).isNotNull();
        }

        private Iterable<String> singleCellExperimentsProvider() {
            return jdbcUtils.fetchPublicExperimentAccessions();
        }
    }
}
