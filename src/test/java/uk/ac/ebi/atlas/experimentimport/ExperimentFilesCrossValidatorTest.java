package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.atlas.model.experiment.sdrf.SampleCharacteristic;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.testutils.AssayGroupFactory;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentFilesCrossValidatorTest {
    @Mock
    private ExperimentConfiguration experimentConfiguration;

    @Mock
    private ExperimentDesign experimentDesign;

    private static final AssayGroup ASSAY_GROUP = AssayGroupFactory.create("g1", "r1", "r2");

    @Before
    public void setUp() {
        when(experimentConfiguration.getAssayGroups()).thenReturn(ImmutableList.of(ASSAY_GROUP));
    }

    @Test(expected = IllegalStateException.class)
    public void missingFactorsThrow() {
        new ExperimentFilesCrossValidator(experimentConfiguration, experimentDesign).factorsNotMissing();
    }

    @Test
    public void presentFactorsPass() {
        when(experimentDesign.getFactorValues("r1")).thenReturn(ImmutableMap.of("type", "value"));
        when(experimentDesign.getFactorValues("r2")).thenReturn(ImmutableMap.of("type", "value"));

        new ExperimentFilesCrossValidator(experimentConfiguration, experimentDesign).factorsNotMissing();
    }


    @Test(expected = IllegalStateException.class)
    public void missingCharacteristicsThrow() {
        new ExperimentFilesCrossValidator(experimentConfiguration, experimentDesign).sampleCharacteristicsNotMissing();
    }

    @Test
    public void presentCharacteristicsPass() {
        when(experimentDesign.getSampleCharacteristics("r1"))
                .thenReturn(ImmutableList.of(SampleCharacteristic.create("", "")));
        when(experimentDesign.getSampleCharacteristics("r2"))
                .thenReturn(ImmutableList.of(SampleCharacteristic.create("", "")));

        new ExperimentFilesCrossValidator(experimentConfiguration, experimentDesign).sampleCharacteristicsNotMissing();
    }
}
