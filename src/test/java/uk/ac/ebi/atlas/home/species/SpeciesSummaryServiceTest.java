package uk.ac.ebi.atlas.home.species;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.species.Species;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(MockitoExtension.class)
class SpeciesSummaryServiceTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int MAX_DIFFERENT_SPECIES = 100;
    private static final int MAX_EXPERIMENTS_PER_TYPE = 500;

    @Mock
    private SpeciesSummaryDao speciesSummaryDaoMock;

    private SpeciesSummaryService subject;

    @BeforeEach
    void setUp() {
        subject = new SpeciesSummaryService(speciesSummaryDaoMock);
    }

    @Test
    void returnsEmptyWhenThereAreNoExperiments() {
        when(speciesSummaryDaoMock.getExperimentCountBySpeciesAndExperimentType()).thenReturn(ImmutableList.of());

        assertThat(subject.getSpeciesSummariesGroupedByKingdom())
                .isEmpty();
    }

    @Test
    void producesTheRightSummaries() {
        var species =
                IntStream.range(0, RNG.nextInt(1, MAX_DIFFERENT_SPECIES)).boxed()
                        .map(__ -> generateRandomSpecies())
                        .collect(toImmutableSet())
                        .asList();

        var experiments =
                species.stream()
                        .map(_species ->
                                Triple.of(
                                        _species,
                                        ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)],
                                        RNG.nextLong(1, MAX_EXPERIMENTS_PER_TYPE)))
                        .collect(toImmutableList());

        when(speciesSummaryDaoMock.getExperimentCountBySpeciesAndExperimentType())
                .thenReturn(experiments);

        assertThat(subject.getSpeciesSummariesGroupedByKingdom().keySet())
                .containsExactlyInAnyOrderElementsOf(
                        species.stream().map(Species::getKingdom).collect(toImmutableSet()));

        var kingdom = species.get(RNG.nextInt(species.size())).getKingdom();
        assertThat(subject.getSpeciesSummariesGroupedByKingdom().get(kingdom))
                .hasSameSizeAs(
                        species.stream()
                                .filter(_species -> _species.getKingdom().equalsIgnoreCase(kingdom))
                                .map(Species::getReferenceName)
                                .collect(toImmutableSet()));

    }
}
