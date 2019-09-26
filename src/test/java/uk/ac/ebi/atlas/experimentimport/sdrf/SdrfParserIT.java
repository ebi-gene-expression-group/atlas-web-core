package uk.ac.ebi.atlas.experimentimport.sdrf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
import uk.ac.ebi.atlas.experimentimport.idf.IdfParser;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SdrfParserIT {
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
        }

        @ParameterizedTest
        @MethodSource("singleCellExperimentsProvider")
        @DisplayName("parses technology type")
        void testIfTechnologyTypePresentInSingleCellExperiment(String experimentAccession) {
            SdrfParser sdrfParser = new SdrfParser(
                    new DataFileHub(dataFilesPath.resolve("scxa")));
            SdrfParserOutput result = sdrfParser.parse(experimentAccession);

            assertThat(result.getTechnologyType()).isNotEmpty();
        }

        private Iterable<String> singleCellExperimentsProvider() {
            return jdbcUtils.fetchPublicExperimentAccessions();
        }
    }
}
