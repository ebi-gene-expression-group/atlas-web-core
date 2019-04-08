package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableSortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;

import java.util.stream.StreamSupport;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentDesignTableTest {
    @Mock
    ExperimentDesign experimentDesignMock;

    private DifferentialExperiment experiment;

    private ExperimentDesignTable subject;

    @BeforeEach
    void setUp() {
        experiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withExperimentDesign(experimentDesignMock)
                        .build();

        when(experimentDesignMock.getAllRunOrAssay())
                .thenReturn(ImmutableSortedSet.copyOf(experiment.getAnalysedAssays()));


        subject = new ExperimentDesignTable(experiment);
    }

    @Test
    void assayIdsAppearInAllContrastsThatContainTheirAssayGroup() {
        var assayIdsInMultipleContrasts =
                experiment.getAnalysedAssays().stream()
                        .filter(assayId ->
                            experiment.getDataColumnDescriptors().stream()
                                    .filter(contrast -> contrast.getAssayIds().contains(assayId))
                                    .count() > 1)
                        .collect(toImmutableSet());

        var result = subject.asJson();

        for (var assayId : assayIdsInMultipleContrasts) {
            assertThat(
                    StreamSupport.stream(result.get("data").getAsJsonArray().spliterator(), false)
                        .filter(jsonElement -> jsonElement.getAsJsonObject().get("values").getAsJsonArray().get(0).getAsString().equals(assayId))
                        .count() > 1)
                    .isTrue();
        }
    }
}