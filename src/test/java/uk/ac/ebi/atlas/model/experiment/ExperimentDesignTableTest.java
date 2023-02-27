package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.experiment.ExperimentDesignTable.JSON_TABLE_MAX_ROWS;

@Disabled
@ExtendWith(MockitoExtension.class)
class ExperimentDesignTableTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Mock
    ExperimentDesign experimentDesignMock;

    private ExperimentDesignTable subject;

    @Test
    void assayIdsAppearInAllContrastsThatContainTheirAssayGroup() {
        var experiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withExperimentDesign(experimentDesignMock)
                        .build();

        when(experimentDesignMock.getAllRunOrAssay())
                .thenReturn(ImmutableSortedSet.copyOf(experiment.getAnalysedAssays()));
        when(experimentDesignMock.getSampleCharacteristicHeaders())
                .thenReturn(ImmutableSet.of());
        when(experimentDesignMock.getFactorHeaders())
                .thenReturn(ImmutableSet.of());

        subject = new ExperimentDesignTable(experiment);


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

    @Test
    void singleCellExperimentDesignTableIsPopulated() {
        var scExperiment =
                new ExperimentBuilder.SingleCellBaselineExperimentBuilder()
                        .withExperimentDesign(experimentDesignMock)
                        .build();

        when(experimentDesignMock.getAllRunOrAssay())
                .thenReturn(ImmutableSortedSet.copyOf(scExperiment.getAnalysedAssays()));
        when(experimentDesignMock.getSampleCharacteristicHeaders())
                .thenReturn(ImmutableSet.of());
        when(experimentDesignMock.getFactorHeaders())
                .thenReturn(ImmutableSet.of());

        subject = new ExperimentDesignTable(scExperiment);

        assertThat(subject.asJson().getAsJsonArray("data")).isNotEmpty();
        assertThat(subject.asJson().getAsJsonArray("data"))
                .allMatch(jsonElement -> !jsonElement.getAsJsonObject().has("analysed"));
    }

    @Test
    void jsonTableIsCappedAt500Rows() {
        var scExperiment =
                new ExperimentBuilder.SingleCellBaselineExperimentBuilder()
                        .withExperimentDesign(experimentDesignMock)
                        .build();
        var assays =
                IntStream.range(1, RNG.nextInt(2, JSON_TABLE_MAX_ROWS * 100)).boxed()
                        .map(__ -> randomAlphanumeric(6, 10))
                        .collect(toImmutableSortedSet(naturalOrder()));

        when(experimentDesignMock.getAllRunOrAssay()).thenReturn(assays);
        when(experimentDesignMock.getSampleCharacteristicHeaders())
                .thenReturn(ImmutableSet.of());
        when(experimentDesignMock.getFactorHeaders())
                .thenReturn(ImmutableSet.of());

        subject = new ExperimentDesignTable(scExperiment);
        assertThat(subject.asJson().getAsJsonArray("data").size()).isLessThanOrEqualTo(JSON_TABLE_MAX_ROWS);
    }
}