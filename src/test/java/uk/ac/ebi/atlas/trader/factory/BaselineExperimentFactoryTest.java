package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateFilterFactors;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroups;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BaselineExperimentFactoryTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private final static int ASSAY_GROUPS_MAX = 10;
    private final static int FACTOR_TYPES_MAX = 5;
    private final static int ALTERNATIVE_VIEWS_MAX = 5;

    private Species species;

    private ExperimentDto experimentDto;
    private IdfParserOutput idfParserOutput;
    private ExperimentDesign experimentDesign;

    @Mock
    private ExperimentConfiguration configurationMock;

    @Mock
    private BaselineExperimentConfiguration baselineConfigurationMock;

    @Mock
    private ConfigurationTrader configurationTraderMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    private BaselineExperimentFactory subject;

    @BeforeEach
    void setUp() {
        String experimentAccession = generateRandomExperimentAccession();
        species = generateRandomSpecies();
        when(speciesFactoryMock.create(species.getName()))
                .thenReturn(species);

        experimentDto = new ExperimentDto(
                experimentAccession,
                ImmutableList.of(RNASEQ_MRNA_BASELINE, PROTEOMICS_BASELINE).get(RNG.nextInt(2)),
                species.getName(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                new Date(),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        idfParserOutput = new IdfParserOutput(
                randomAlphabetic(20),
                randomAlphabetic(20),
                randomAlphabetic(100),
                ImmutableList.of(),
                RNG.nextInt(20),
                ImmutableList.of());

        experimentDesign = new ExperimentDesign();

        when(configurationMock.getAssayGroups())
                .thenReturn(generateRandomAssayGroups(RNG.nextInt(1, ASSAY_GROUPS_MAX)));
        when(configurationTraderMock.getExperimentConfiguration(experimentAccession))
                .thenReturn(configurationMock);

        int factorTypeCount = RNG.nextInt(1, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypeCount, RNG.nextInt(factorTypeCount));

        when(baselineConfigurationMock.getDisclaimer()).thenReturn("");
        when(baselineConfigurationMock.getDefaultFilterFactors()).thenReturn(filterFactors.getRight());
        when(baselineConfigurationMock.getDefaultQueryFactorType()).thenReturn(filterFactors.getLeft());
        when(baselineConfigurationMock.getMenuFilterFactorTypes()).thenReturn(filterFactors.getMiddle().asList());
        when(baselineConfigurationMock.isOrderCurated()).thenReturn(RNG.nextBoolean());
        when(baselineConfigurationMock.getDataProviderUrl()).thenReturn(ImmutableList.of());
        when(baselineConfigurationMock.getDataProviderDescription()).thenReturn(ImmutableList.of());
        when(baselineConfigurationMock.getExperimentDisplayName()).thenReturn(randomAlphabetic(20));

        when(configurationTraderMock.getBaselineFactorsConfiguration(experimentAccession))
                .thenReturn(baselineConfigurationMock);

        subject = new BaselineExperimentFactory(configurationTraderMock, speciesFactoryMock);
    }

    // ExperimentDto comes from DB
    // IdfParserOutput comes from IDF file
    // ExperimentConfiguration comes from <exp_accession>-configuration.xml
    // BaselineExperimentConfiguration comes from <exp_accession>-factors.xml
    @Test
    void experimentIsProperlyPopulatedFromDatabaseIdfFactorsAndConfiguration() {
        assertThat(subject.create(experimentDto, experimentDesign, idfParserOutput))
                .isInstanceOf(BaselineExperiment.class)
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
                        configurationMock.getAssayGroups(),
                        experimentDesign,
                        experimentDto.getPubmedIds(),
                        experimentDto.getDois(),
                        baselineConfigurationMock.getExperimentDisplayName(),
                        baselineConfigurationMock.getDisclaimer(),
                        baselineConfigurationMock.getDataProviderUrl(),
                        baselineConfigurationMock.getDataProviderDescription(),
                        experimentDto.isPrivate());
    }

    @Test
    void alternativeViewsAreLabelledByTheirDefaultQueryFactorType() {
        ImmutableMap<String, String> accession2DefaultQueryFactorType=
                IntStream.rangeClosed(1, ALTERNATIVE_VIEWS_MAX).boxed()
                        .collect(toImmutableMap(
                                __ -> generateRandomExperimentAccession(),
                                __ -> randomAlphabetic(5, 10).toUpperCase()));

        accession2DefaultQueryFactorType.forEach(
                (accession, factorType) -> {
                    BaselineExperimentConfiguration baselineExperimentConfigurationMock =
                            mock(BaselineExperimentConfiguration.class);
                    when(baselineExperimentConfigurationMock.getDefaultQueryFactorType())
                            .thenReturn(factorType);
                    when(configurationTraderMock.getBaselineFactorsConfiguration(accession))
                            .thenReturn(baselineExperimentConfigurationMock);
                });

        when(baselineConfigurationMock.getAlternativeViews())
                .thenReturn(ImmutableList.copyOf(accession2DefaultQueryFactorType.keySet()));

        assertThat(subject.create(experimentDto, experimentDesign, idfParserOutput).getAlternativeViews())
                .hasSameElementsAs(accession2DefaultQueryFactorType.keySet());
        assertThat(subject.create(experimentDto, experimentDesign, idfParserOutput).getAlternativeViewDescriptions())
                .hasSameElementsAs(
                        accession2DefaultQueryFactorType.values().stream()
                            .map(factorType -> "View by " + factorType.toLowerCase())
                            .collect(toImmutableSet()));
    }

    @Test
    void throwIfExperimentTypeIsNotBaseline() {
        experimentDto = new ExperimentDto(
                generateRandomExperimentAccession(),
                Arrays.stream(ExperimentType.values())
                        .filter(type -> !type.isBaseline())
                        .findAny()
                        .orElseThrow(RuntimeException::new),
                species.getName(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                new Date(),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        assertThatIllegalArgumentException().isThrownBy(
                () -> subject.create(experimentDto, experimentDesign, idfParserOutput));
    }
}
