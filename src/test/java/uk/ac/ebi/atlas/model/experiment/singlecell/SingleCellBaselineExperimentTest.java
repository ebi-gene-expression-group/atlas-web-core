package uk.ac.ebi.atlas.model.experiment.singlecell;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.SingleCellBaselineExperimentBuilder;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;

class SingleCellBaselineExperimentTest {
    @Test
    void propertiesForAssayAreAlwaysEmpty() {
        SingleCellBaselineExperiment subject = new SingleCellBaselineExperimentBuilder().build();

        assertThat(
                subject.propertiesForAssay(subject.getAnalysedAssays().iterator().next()))
                .isEqualTo(subject.propertiesForAssay(randomAlphanumeric(10)))
                .isEqualTo(subject.propertiesForAssay(generateBlankString()))
                .isEmpty();
    }
}