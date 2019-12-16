package uk.ac.ebi.atlas.experiments;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.atlas.testutils.MockExperiment;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentJsonServiceTest {
    private static final String EXPERIMENT_ACCESSION = generateRandomExperimentAccession();

    @Mock
    private ExperimentTrader experimentTraderMock;

    private ExperimentJsonService subject;

    @Before
    public void setUp() throws Exception {
        when(experimentTraderMock.getPublicExperiments())
                .thenReturn(ImmutableSet.of(MockExperiment.createBaselineExperiment(EXPERIMENT_ACCESSION)));

        when(experimentTraderMock.getPublicExperiments("sex", "female"))
                .thenReturn(ImmutableSet.of(MockExperiment.createBaselineExperiment(EXPERIMENT_ACCESSION)));

        subject = new ExperimentJsonService(experimentTraderMock);
    }

    @Test
    public void sizeIsRightForNonParameterisedExperimentJsonMethod() {
        assertThat(subject.getPublicExperimentsJson()).hasSize(1);
    }

    @Test
    public void sizeIsRightForCorrectCharacteristicNameAndCharacteristicValue() {
        assertThat(subject.getPublicExperimentsJson("sex","female")).hasSize(1);
    }

    @Test
    public void sizeIsRightForEmptyCharacteristicName() {
        assertThat(subject.getPublicExperimentsJson("", "female")).hasSize(1);
    }

    @Test
    public void sizeIsRightForEmptyCharacteristicValueJsonMethod() {
        assertThat(subject.getPublicExperimentsJson("sex", "")).hasSize(1);
    }

    @Test
    public void sizeIsRightForEmptyCharacteristicNameAndCharacteristicValue() {
        assertThat(subject.getPublicExperimentsJson("", "")).hasSize(1);
    }

    @Test
    public void formatIsInSyncWithWhatWeExpectAndTheDataOfMockBaselineExperiment() {
        var result = subject.getPublicExperimentsJson().iterator().next();

        assertThat(result.get("experimentType").getAsString()).isEqualTo("Baseline");
        assertThat(result.get("experimentAccession").getAsString()).isEqualToIgnoringCase(EXPERIMENT_ACCESSION);
        assertThat(result.get("experimentDescription").getAsString()).isNotEmpty();
        assertThat(result.get("loadDate").getAsString()).isNotEmpty();
        assertThat(result.get("lastUpdate").getAsString()).isNotEmpty();
        assertThat(result.get("numberOfAssays").getAsInt()).isGreaterThan(0);

        assertThat(result.get("species").getAsString()).isNotEmpty();
        assertThat(result.get("kingdom").getAsString()).isNotEmpty();

        assertThat(result.has("experimentalFactors")).isTrue();
    }

    @Test
    public void formatIsAsExpectedForCorrectCharacteristicNameAndValue() {
        var result = subject.getPublicExperimentsJson("sex","female").iterator().next();

        assertThat(result.get("experimentType").getAsString()).isEqualTo("Baseline");
        assertThat(result.get("experimentAccession").getAsString()).isEqualToIgnoringCase(EXPERIMENT_ACCESSION);
        assertThat(result.get("experimentDescription").getAsString()).isNotEmpty();
        assertThat(result.get("loadDate").getAsString()).isNotEmpty();
        assertThat(result.get("lastUpdate").getAsString()).isNotEmpty();
        assertThat(result.get("numberOfAssays").getAsInt()).isGreaterThan(0);

        assertThat(result.get("species").getAsString()).isNotEmpty();
        assertThat(result.get("kingdom").getAsString()).isNotEmpty();

        assertThat(result.has("experimentalFactors")).isTrue();
    }

    @Test
    public void formatIsAsExpectedForEmptyCharacteristicName() {
        var result = subject.getPublicExperimentsJson().iterator().next();

        assertThat(result.get("experimentType").getAsString()).isEqualTo("Baseline");
        assertThat(result.get("experimentAccession").getAsString()).isEqualToIgnoringCase(EXPERIMENT_ACCESSION);
        assertThat(result.get("experimentDescription").getAsString()).isNotEmpty();
        assertThat(result.get("loadDate").getAsString()).isNotEmpty();
        assertThat(result.get("lastUpdate").getAsString()).isNotEmpty();
        assertThat(result.get("numberOfAssays").getAsInt()).isGreaterThan(0);

        assertThat(result.get("species").getAsString()).isNotEmpty();
        assertThat(result.get("kingdom").getAsString()).isNotEmpty();

        assertThat(result.has("experimentalFactors")).isTrue();
    }

    @Test
    public void formatIsAsExpectedForEmptyCharacteristicValue() {
        var result = subject.getPublicExperimentsJson("sex","").iterator().next();

        assertThat(result.get("experimentType").getAsString()).isEqualTo("Baseline");
        assertThat(result.get("experimentAccession").getAsString()).isEqualToIgnoringCase(EXPERIMENT_ACCESSION);
        assertThat(result.get("experimentDescription").getAsString()).isNotEmpty();
        assertThat(result.get("loadDate").getAsString()).isNotEmpty();
        assertThat(result.get("lastUpdate").getAsString()).isNotEmpty();
        assertThat(result.get("numberOfAssays").getAsInt()).isGreaterThan(0);

        assertThat(result.get("species").getAsString()).isNotEmpty();
        assertThat(result.get("kingdom").getAsString()).isNotEmpty();

        assertThat(result.has("experimentalFactors")).isTrue();
    }

    @Test
    public void formatIsAsExpectedForEmptyCharacteristicNameAndValue() {
        var result = subject.getPublicExperimentsJson("","").iterator().next();

        assertThat(result.get("experimentType").getAsString()).isEqualTo("Baseline");
        assertThat(result.get("experimentAccession").getAsString()).isEqualToIgnoringCase(EXPERIMENT_ACCESSION);
        assertThat(result.get("experimentDescription").getAsString()).isNotEmpty();
        assertThat(result.get("loadDate").getAsString()).isNotEmpty();
        assertThat(result.get("lastUpdate").getAsString()).isNotEmpty();
        assertThat(result.get("numberOfAssays").getAsInt()).isGreaterThan(0);

        assertThat(result.get("species").getAsString()).isNotEmpty();
        assertThat(result.get("kingdom").getAsString()).isNotEmpty();

        assertThat(result.has("experimentalFactors")).isTrue();
    }
}
