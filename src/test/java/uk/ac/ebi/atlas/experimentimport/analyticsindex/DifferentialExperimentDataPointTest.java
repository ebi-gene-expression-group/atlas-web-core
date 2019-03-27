package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import org.junit.Test;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.DifferentialExperimentDataPoint;
import uk.ac.ebi.atlas.experimentimport.analytics.differential.rnaseq.RnaSeqDifferentialAnalytics;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.DifferentialExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class DifferentialExperimentDataPointTest {
    @Test
    public void testGetRelevantBioentityPropertyNames() {
        DifferentialExperimentDataPoint subject =
                new DifferentialExperimentDataPoint(
                        new DifferentialExperimentBuilder().build(),
                        new RnaSeqDifferentialAnalytics("", "", 0.03, 1.23), "", 5);

        assertThat(subject.getProperties())
                .containsKeys("factors", "regulation", "contrast_id", "num_replicates", "fold_change", "p_value")
                .doesNotContainKeys(
                        "expression_level",
                        "expression_level_fpkm",
                        "expression_levels",
                        "expression_levels_fpkm",
                        "assay_group_id");
    }
}
