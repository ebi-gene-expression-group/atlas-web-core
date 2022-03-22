package uk.ac.ebi.atlas.experiments;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperimentCellCountDaoIT {
    @Inject
    private DataSource dataSource;

    @Inject
    private JdbcUtils jdbcTestUtils;

    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ExperimentCellCountDao subject;

    public ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

    @BeforeAll
    void populateDatabaseTables() {
        populator.setScripts(
                new ClassPathResource("fixtures/scxa/experiment.sql"),
                new ClassPathResource("fixtures/scxa/scxa_cell_group.sql"),
                new ClassPathResource("fixtures/scxa/scxa_cell_group_membership.sql"));
        populator.execute(dataSource);
    }

    @AfterAll
    void cleanDatabaseTables() {
        populator.setScripts(
                new ClassPathResource("fixtures/scxa/scxa_cell_group_membership-delete.sql"),
                new ClassPathResource("fixtures/scxa/scxa_cell_group-delete.sql"),
                new ClassPathResource("fixtures/scxa/experiment-delete.sql"));
        populator.execute(dataSource);
    }

    @BeforeEach
    void setUp() {
        this.subject = new ExperimentCellCountDao(namedParameterJdbcTemplate);
    }

    @Test
    void testForNumberOfCells() {
        assertThat(subject.fetchNumberOfCellsByExperimentAccession(jdbcTestUtils.fetchRandomExperimentAccession()))
                .isGreaterThan(1)
                .isCloseTo(2, withinPercentage(10));
    }
}