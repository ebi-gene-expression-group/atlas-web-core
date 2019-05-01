package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperimentConfiguration;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class BaselineExperimentFactory implements ExperimentFactory<BaselineExperiment> {
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;

    public BaselineExperimentFactory(@NotNull ConfigurationTrader configurationTrader,
                                     @NotNull SpeciesFactory speciesFactory) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
    }

    @Override
    @NotNull
    public BaselineExperiment create(@NotNull ExperimentDto experimentDto,
                                     @NotNull ExperimentDesign experimentDesign,
                                     @NotNull IdfParserOutput idfParserOutput) {
        checkArgument(
                experimentDto.getExperimentType().isBaseline(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type baseline");

        var configuration = configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());
        var factorsConfig = configurationTrader.getBaselineFactorsConfiguration(experimentDto.getExperimentAccession());
        var alternativeViews = extractAlternativeViews(factorsConfig);

        return new BaselineExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getSecondaryAccession(),
                idfParserOutput.getTitle(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                configuration.getAssayGroups(),
                experimentDesign,
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                factorsConfig.getExperimentDisplayName(),
                factorsConfig.getDisclaimer(),
                factorsConfig.getDataProviderUrl(),
                factorsConfig.getDataProviderDescription(),
                alternativeViews.getLeft(),
                alternativeViews.getRight(),
                ExperimentDisplayDefaults.create(
                        factorsConfig.getDefaultQueryFactorType(),
                        factorsConfig.getDefaultFilterFactors(),
                        factorsConfig.getMenuFilterFactorTypes(),
                        factorsConfig.isOrderCurated()),
                experimentDto.isPrivate());
    }

    @NotNull
    private ImmutablePair<@NotNull ImmutableList<String>, @NotNull ImmutableList<String>>
    extractAlternativeViews(@NotNull BaselineExperimentConfiguration factorsConfig) {
        return ImmutablePair.of(
                ImmutableList.copyOf(factorsConfig.getAlternativeViews()),
                factorsConfig.getAlternativeViews().stream()
                    .map(altViewAccession ->
                            "View by " +
                                    configurationTrader.getBaselineFactorsConfiguration(altViewAccession)
                                            .getDefaultQueryFactorType()
                                            .toLowerCase()
                                            .replace("_", " "))
                    .collect(toImmutableList()));
    }
}
