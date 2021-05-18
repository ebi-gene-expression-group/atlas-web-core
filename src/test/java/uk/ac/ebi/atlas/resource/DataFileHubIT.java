package uk.ac.ebi.atlas.resource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DataFileHubIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileHubIT.class);

    @Nested
    @Transactional(transactionManager = "txManager")
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
            var populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/gxa-experiment-fixture.sql"));
            populator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            var populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
            populator.execute(dataSource);
        }

        @Test
        void testGetExperimentFiles() {
            var subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            LOGGER.info("Test experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).analysisMethods);
            assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).condensedSdrf);
            assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).experimentDesign);
        }

        @Test
        void testGetBaselineFiles() {
            var subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession(ExperimentType.RNASEQ_MRNA_BASELINE);
            LOGGER.info("Test baseline experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(
                    subject.getRnaSeqBaselineExperimentFiles(experimentAccession)
                            .dataFile(ExpressionUnit.Absolute.Rna.TPM));
            assertAtlasResourceExists(
                    subject.getRnaSeqBaselineExperimentFiles(experimentAccession)
                            .dataFile(ExpressionUnit.Absolute.Rna.FPKM));
        }

        @Test
        void testGetProteomicsBaselineFiles() {
            var subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession(ExperimentType.PROTEOMICS_BASELINE);
            LOGGER.info("Test proteomics baseline experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(subject.getProteomicsBaselineExperimentFiles(experimentAccession).main);
        }

        @Test
        void testGetDifferentialExperimentFiles() {
            var subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession(ExperimentType.RNASEQ_MRNA_DIFFERENTIAL);
            LOGGER.info("Test differential experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(subject.getRnaSeqDifferentialExperimentFiles(experimentAccession).analytics);
            assertAtlasResourceExists(subject.getRnaSeqDifferentialExperimentFiles(experimentAccession).rawCounts);
        }
    }

    private static void assertAtlasResourceExists(AtlasResource<?> resource) {
        assertThat(resource.exists()).isTrue();
    }
}
