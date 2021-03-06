package uk.ac.ebi.atlas.resource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Collection;

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

        private DataFileHub subject;

        @BeforeAll
        void populateDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/gxa-experiment-fixture.sql"));
            populator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
            populator.execute(dataSource);
        }

        @Test
        void testGetExperimentFiles() {
            subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            String experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            LOGGER.info("Test experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).analysisMethods);
            assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).condensedSdrf);
            assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).experimentDesign);
        }

        @Test
        void testGetBaselineFiles() {
            subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            String experimentAccession =
                    jdbcUtils.fetchRandomExperimentAccession(ExperimentType.RNASEQ_MRNA_BASELINE);
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
            subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            String experimentAccession =
                    jdbcUtils.fetchRandomExperimentAccession(ExperimentType.PROTEOMICS_BASELINE);
            LOGGER.info("Test proteomics baseline experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(subject.getProteomicsBaselineExperimentFiles(experimentAccession).main);
        }

        @Test
        void testGetDifferentialExperimentFiles() {
            subject = new DataFileHub(dataFilesPath.resolve("gxa"));
            String experimentAccession =
                    jdbcUtils.fetchRandomExperimentAccession(ExperimentType.RNASEQ_MRNA_DIFFERENTIAL);
            LOGGER.info("Test differential experiment files for experiment {}", experimentAccession);

            assertAtlasResourceExists(subject.getRnaSeqDifferentialExperimentFiles(experimentAccession).analytics);
            assertAtlasResourceExists(subject.getRnaSeqDifferentialExperimentFiles(experimentAccession).rawCounts);
        }
    }

    @Nested
    @Transactional(transactionManager = "txManager")
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

        private DataFileHub subject;

        @BeforeAll
        void populateDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/scxa-experiment-fixture.sql"));
            populator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
            populator.execute(dataSource);
        }

        @Test
        void findsTSnePlotFiles() {
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            subject = new DataFileHub(dataFilesPath.resolve("scxa"));
            LOGGER.info("Test tsne plot files for experiment {}", experimentAccession);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).tSnePlotTsvs.values());
        }

        @Test
        void findsMarkerGeneFiles() {
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            var subject = new DataFileHub(dataFilesPath.resolve("scxa"));
            LOGGER.info("Test marker gene files for experiment {}", experimentAccession);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).markerGeneTsvs.values());
        }

        @Test
        void findsCellTypeMarkerGeneFiles(@Value("${data.files.location}") String dataFilesLocation) {
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            var subject = new DataFileHub(dataFilesPath.resolve("scxa"));
            LOGGER.info("Test cell type marker gene files for experiment {}", experimentAccession);
            assertThat(subject.getSingleCellExperimentFiles(experimentAccession).markerGeneTsvs.values()
                    .stream().map(path -> path.getPath()))
                    .contains(Path.of(dataFilesLocation +
                            "/scxa/magetab/" + experimentAccession + "/" + experimentAccession +
                            ".marker_genes_inferred_cell_type_-_ontology_labels.tsv"));
        }

        @Test
        void findsRawFilteredCountsFiles() {
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            var subject = new DataFileHub(dataFilesPath.resolve("scxa"));
            LOGGER.info("Test raw filtered count files for experiment {}", experimentAccession);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).filteredCountsMatrix);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).filteredCountsGeneIdsTsv);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).filteredCountsCellIdsTsv);
        }

        @Test
        void findsNormalisedCountsFiles() {
            var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
            var subject = new DataFileHub(dataFilesPath.resolve("scxa"));
            LOGGER.info("Test normalised filtered count files for experiment {}", experimentAccession);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).normalisedCountsMatrix);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).normalisedCountsGeneIdsTsv);
            assertAtlasResourceExists(subject.getSingleCellExperimentFiles(experimentAccession).normalisedCountsCellIdsTsv);
        }
    }

    private static void assertAtlasResourceExists(AtlasResource<?> resource) {
        assertThat(resource.exists()).isTrue();
    }

    private static void assertAtlasResourceExists(Collection<? extends AtlasResource<?>> resource) {
        assertThat(resource).isNotEmpty();
        assertThat(resource).allMatch(AtlasResource::exists);
    }
}
