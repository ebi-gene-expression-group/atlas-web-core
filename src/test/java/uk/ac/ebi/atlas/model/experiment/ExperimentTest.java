package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesProperties;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import uk.ac.ebi.atlas.testutils.ExperimentBuilder.TestExperimentBuilder;
import uk.ac.ebi.atlas.testutils.TestExperiment;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

public class ExperimentTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void throwIfExperimentAccessionIsBlank() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder().withExperimentAccession(generateBlankString()).build());
    }

    @Test
    void throwIfDescriptionIsBlank() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder().withExperimentDescription(generateBlankString()).build());
    }

    @Test
    void throwIfSpeciesIsUnknown() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder()
                        .withSpecies(new Species(randomAlphabetic(10), SpeciesProperties.UNKNOWN))
                        .build());
    }

    @Test
    void throwIfExpressedSamplesIsEmpty() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder().withSamples(ImmutableList.of()).build());
    }

    @Test
    void throwIfDataProviderDescriptionsAndUrlsDontMatch() {
        int dataProviderDescriptionsSize = RNG.nextInt(10);
        int dataProviderUrlsSize = dataProviderDescriptionsSize;
        while (dataProviderUrlsSize == dataProviderDescriptionsSize) {
            dataProviderDescriptionsSize = RNG.nextInt(10);
        }

        ImmutableList<String> dataProviderDescriptions =
                IntStream.range(0, dataProviderDescriptionsSize).boxed()
                        .map(__ -> randomAlphabetic(40))
                        .collect(toImmutableList());

        ImmutableList<String> dataProviderUrls =
                IntStream.range(0, dataProviderUrlsSize).boxed()
                        .map(__ -> "https://www." + randomAlphabetic(4, 10) + ".org/" + randomAlphabetic(0, 10))
                        .collect(toImmutableList());

        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder()
                        .withDataProviderDescriptions(dataProviderDescriptions)
                        .withDataProviderUrls(dataProviderUrls)
                        .build());
    }

    @Test
    void throwIfAccessKeyIsBlank() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder().withAccessKey(generateBlankString()).build());
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder().withAccessKey(null).build());
    }

    @Test
    void throwIfAlternativeViewsAndDescriptionsDontMatch() {
        int alternativeViewsSize = RNG.nextInt(10);
        int alternativeViewDescriptionsSize = alternativeViewsSize;
        while (alternativeViewDescriptionsSize == alternativeViewsSize) {
            alternativeViewDescriptionsSize = RNG.nextInt(10);
        }

        ImmutableList<String> alternativeViews =
                IntStream.range(0, alternativeViewsSize).boxed()
                        .map(__ -> generateRandomExperimentAccession())
                        .collect(toImmutableList());

        ImmutableList<String> alternativeViewDescriptions =
                IntStream.range(0, alternativeViewDescriptionsSize).boxed()
                        .map(__ -> randomAlphabetic(10, 40))
                        .collect(toImmutableList());

        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestExperimentBuilder()
                        .withAlternativeViews(alternativeViews)
                        .withAlternativeViewDescriptions(alternativeViewDescriptions)
                        .build());
    }

    @Test
    void testGetters() {
        TestExperimentBuilder builder = new TestExperimentBuilder();
        TestExperiment subject = builder.build();

        assertThat(subject)
                .hasFieldOrPropertyWithValue("type", builder.experimentType)
                .hasFieldOrPropertyWithValue("accession", builder.experimentAccession)
                .hasFieldOrPropertyWithValue("description", builder.experimentDescription)
                .hasFieldOrPropertyWithValue("loadDate", builder.loadDate)
                .hasFieldOrPropertyWithValue("lastUpdate", builder.lastUpdate)
                .hasFieldOrPropertyWithValue("species", builder.species)
                .hasFieldOrPropertyWithValue("displayName", builder.displayName)
                .hasFieldOrPropertyWithValue("disclaimer", builder.disclaimer)
                .hasFieldOrPropertyWithValue("displayDefaults", builder.experimentDisplayDefaults)
                .hasFieldOrPropertyWithValue("private", builder.isPrivate)
                .hasFieldOrPropertyWithValue("accessKey", builder.accessKey);


        assertThat(subject.getAlternativeViews())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(builder.alternativeViews));
        assertThat(subject.getAlternativeViewDescriptions())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(builder.alternativeViewDescriptions));

        assertThat(subject.getDataProviderURL())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(builder.dataProviderUrls));
        assertThat(subject.getDataProviderDescription())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(builder.dataProviderDescriptions));

        assertThat(subject.getPubMedIds())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(builder.pubMedIds));
        assertThat(subject.getDois())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(builder.dois));

        assertThat(subject.getAnalysedAssays())
                .containsExactlyInAnyOrderElementsOf(
                        builder.samples.stream()
                                .flatMap(sample -> sample.getAssayIds().stream())
                                .collect(toImmutableSet()));

        assertThat(subject.getDataColumnDescriptors())
                .containsExactlyInAnyOrderElementsOf(builder.samples);
        TestExperiment.TestSample randomSample = builder.samples.get(RNG.nextInt(builder.samples.size()));
        assertThat(subject.getDataColumnDescriptor(randomSample.getId()))
                .isEqualTo(randomSample);

        assertThat(subject.getAnalysedAssays())
                .containsExactlyInAnyOrderElementsOf(
                        builder.samples.stream()
                                .flatMap(sample -> sample.getAssayIds().stream())
                                .collect(toImmutableSet()));
    }

    @Test
    void equalsIsConsistentWithHashCode() {
        String experimentAccession = generateRandomExperimentAccession();
        TestExperiment experiment1 =
                new TestExperimentBuilder().withExperimentAccession(experimentAccession).build();
        TestExperiment experiment2 =
                new TestExperimentBuilder().withExperimentAccession(experimentAccession).build();

        assertThat(experiment1)
                .hasSameHashCodeAs(experiment2)
                .isEqualTo(experiment2)
                .isEqualTo(experiment1)
                .isNotEqualTo(null);
    }

    @Test
    void microRnaExperimentsHaveNoAvailableGenomeBrowser() {
        Species species = generateRandomSpecies();
        TestExperiment subject =
                new TestExperimentBuilder()
                        .withExperimentType(MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL)
                        .withSpecies(species)
                        .build();

        assertThat(species.getGenomeBrowsers())
                .isNotEmpty();
        assertThat(subject)
                .hasFieldOrPropertyWithValue("species", species)
                .hasFieldOrPropertyWithValue("genomeBrowsers", ImmutableList.of());
        assertThat(subject.getGenomeBrowsers())
                .isEmpty();
        assertThat(subject.getGenomeBrowserNames())
                .isEmpty();
    }

    @Test
    void genomeBrowsers() {
        Species species = generateRandomSpecies();

        ExperimentType nonMiRnaExperimentType;
        do {
            nonMiRnaExperimentType = ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)];
        } while (nonMiRnaExperimentType == MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL);

        TestExperiment subject =
                new TestExperimentBuilder().withExperimentType(nonMiRnaExperimentType).withSpecies(species).build();

        assertThat(subject)
                .hasFieldOrPropertyWithValue(
                        "genomeBrowsers",
                        species.getGenomeBrowsers());
        assertThat(subject.getGenomeBrowserNames())
                .containsExactlyInAnyOrderElementsOf(
                        species.getGenomeBrowsers().stream().map(map -> map.get("name")).collect(toImmutableList()));
    }
}