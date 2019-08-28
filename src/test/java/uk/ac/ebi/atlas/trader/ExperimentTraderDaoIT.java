package uk.ac.ebi.atlas.trader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class ExperimentTraderDaoIT {
    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private ExperimentTraderDao subject;

    @Sql("/fixtures/gxa-experiment-fixture.sql")
    @Test
    void ifNoTypeIsProvidedReturnAllExperiment() {
        assertThat(subject.fetchPublicExperimentAccessions())
                .isNotEmpty()
                .size().isEqualTo(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "experiment", "private=FALSE"));
    }

    @Sql("/fixtures/scxa-experiment-fixture.sql")
    @Test
    void emptyIfNoExperimentsCanBeFound() {
        assertThat(subject.fetchPublicExperimentAccessions(SINGLE_CELL_RNASEQ_MRNA_BASELINE))
                .isNotEmpty()
                .size().isEqualTo(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "experiment", "private=FALSE"));
        assertThat(subject.fetchPublicExperimentAccessions(PROTEOMICS_BASELINE))
                .isEmpty();
    }

    @Sql({"/fixtures/gxa-experiment-fixture.sql", "/fixtures/scxa-experiment-fixture.sql"})
    @Test
    void getSpecificTypeOfExperiments() {
        assertThat(ExperimentType.values())
                .allSatisfy(experimentType ->
                    assertThat(subject.fetchPublicExperimentAccessions(experimentType))
                            .isNotEmpty()
                            .size().isLessThan(
                                    JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "experiment", "private=FALSE")));
    }
}
