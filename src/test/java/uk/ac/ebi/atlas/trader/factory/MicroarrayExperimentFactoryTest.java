package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.experimentimport.sdrf.SdrfParserOutput;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesignDao;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomContrasts;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MicroarrayExperimentFactoryTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private final static int CONTRASTS_MAX = 10;

    private Species species;

    private ExperimentDto experimentDto;
    private IdfParserOutput idfParserOutput;
    private ExperimentDesign experimentDesign;
    private SdrfParserOutput sdrfParserOutput;

    @Mock
    private ExperimentConfiguration configurationMock;

    @Mock
    private ConfigurationTrader configurationTraderMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    @Mock
    private ArrayDesignDao arrayDesignDaoMock;

    private MicroarrayExperimentFactory subject;

    @BeforeEach
    void setUp() {
        String experimentAccession = generateRandomExperimentAccession();
        species = generateRandomSpecies();
        when(speciesFactoryMock.create(species.getName()))
                .thenReturn(species);

        experimentDto = new ExperimentDto(
                experimentAccession,
                Arrays.stream(ExperimentType.values())
                        .filter(ExperimentType::isMicroarray)
                        .findAny()
                        .orElseThrow(RuntimeException::new),
                species.getName(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                new Timestamp(new Date().getTime()),
                new Timestamp(new Date().getTime()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        idfParserOutput = new IdfParserOutput(
                randomAlphabetic(20),
                randomAlphabetic(20),
                randomAlphabetic(100),
                ImmutableList.of(),
                RNG.nextInt(20),
                ImmutableList.of());

        sdrfParserOutput = new SdrfParserOutput(
                Optional.of(Arrays.asList(randomAlphabetic(20), randomAlphabetic(20)))
        );

        experimentDesign = new ExperimentDesign();

        when(configurationTraderMock.getExperimentConfiguration(experimentAccession))
                .thenReturn(configurationMock);

        subject = new MicroarrayExperimentFactory(configurationTraderMock, speciesFactoryMock, arrayDesignDaoMock);
    }

    // ExperimentDto comes from DB
    // IdfParserOutput comes from IDF file
    // ExperimentConfiguration comes from <exp_accession>-configuration.xml
    @Test
    void experimentIsProperlyPopulatedFromDatabaseIdfFactorsAndConfiguration() {
        ImmutableList<Contrast> contrasts = generateRandomContrasts(RNG.nextInt(1, CONTRASTS_MAX), true);
        ImmutableMap<String, String> arrayDesigns2ArrayNames =
                contrasts.stream()
                        .map(Contrast::getArrayDesignAccession)
                        .collect(toImmutableMap(
                                Function.identity(),
                                __ -> randomAlphanumeric(40, 80),
                                (v1, v2) -> v2));   // If there are different names pick one, we don’t care which
        arrayDesigns2ArrayNames.forEach(
                (arrayDesignAccession, arrayDesignName) ->
                        when(arrayDesignDaoMock.getArrayDesign(arrayDesignAccession))
                                .thenReturn(ArrayDesign.create(arrayDesignAccession, arrayDesignName)));

        when(configurationMock.getContrastAndAnnotationPairs())
                .thenReturn(
                        contrasts.stream()
                                .map(contrast -> Pair.of(contrast, RNG.nextBoolean()))
                                .collect(toImmutableList()));
        when(configurationMock.getArrayDesignAccessions())
                .thenReturn(ImmutableSortedSet.copyOf(arrayDesigns2ArrayNames.keySet()));

        MicroarrayExperiment result = subject.create(experimentDto, experimentDesign, idfParserOutput, sdrfParserOutput);
        assertThat(result)
                .extracting(
                        "type",
                        "description",
                        "lastUpdate",
                        "species",
                        "dataColumnDescriptors",
                        "experimentDesign",
                        "pubMedIds",
                        "dois",
                        "displayName",
                        "disclaimer",
                        "dataProviderUrls",
                        "dataProviderDescriptions",
                        "private")
                .containsExactly(
                        experimentDto.getExperimentType(),
                        idfParserOutput.getTitle(),
                        experimentDto.getLastUpdate(),
                        species,
                        contrasts,
                        experimentDesign,
                        experimentDto.getPubmedIds(),
                        experimentDto.getDois(),
                        experimentDto.getExperimentAccession(),
                        "",
                        ImmutableList.of(),
                        ImmutableList.of(),
                        experimentDto.isPrivate());
        assertThat(result.getArrayDesignAccessions())
                .hasSameElementsAs(arrayDesigns2ArrayNames.keySet());
        assertThat(result.getArrayDesignNames())
                .hasSameElementsAs(arrayDesigns2ArrayNames.values());
    }

    @Test
    void throwIfExperimentTypeIsNotMicroarray() {
        experimentDto = new ExperimentDto(
                generateRandomExperimentAccession(),
                Arrays.stream(ExperimentType.values())
                        .filter(type -> !type.isMicroarray())
                        .findAny()
                        .orElseThrow(RuntimeException::new),
                species.getName(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                new Timestamp(new Date().getTime()),
                new Timestamp(new Date().getTime()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        assertThatIllegalArgumentException().isThrownBy(
                () -> subject.create(experimentDto, experimentDesign, idfParserOutput, sdrfParserOutput));
    }
}
