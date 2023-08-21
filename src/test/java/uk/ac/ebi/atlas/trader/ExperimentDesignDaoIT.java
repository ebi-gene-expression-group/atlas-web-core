package uk.ac.ebi.atlas.trader;

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
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ExperimentDesignDaoIT {

    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Inject
    private ExperimentDesignDao subject;

    @Inject
    private DataSource dataSource;

    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @BeforeAll
    void populateDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("fixtures/scxa/experiment.sql"),
                new ClassPathResource("fixtures/scxa/scxa_exp_design_column.sql"),
                new ClassPathResource("fixtures/scxa/scxa_exp_design.sql"));
        populator.execute(dataSource);
    }

    @AfterAll
    void cleanDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("fixtures/scxa/scxa_exp_design-delete.sql"),
                new ClassPathResource("fixtures/scxa/scxa_exp_design_column-delete.sql"),
                new ClassPathResource("fixtures/scxa/experiment-delete.sql"));
        populator.execute(dataSource);
    }

    @BeforeEach
    void setUp() {
        this.subject = new ExperimentDesignDao(namedParameterJdbcTemplate);
    }

    @Test
    void getTotalNumberOfRows() {
        assertThat(subject.getTableSize("E-EHCA-2"))
                .isNotZero()
                .isNotNegative();
    }

    @Test
    void getNumberOfColumns() {
        assertThat(subject.getColumnHeaders("E-EHCA-2"))
                .isNotEmpty()
                .hasSize(2)
                .containsOnlyKeys("characteristic", "factor");
    }

    @Test
    void getExperimentDesignData() {
        var pageNo = RNG.nextInt(1,10);
        var pageSize = RNG.nextInt(20,50);

        assertThat(subject.getExperimentDesignData("E-EHCA-2", pageNo, pageSize))
                .isNotNull();
    }
}