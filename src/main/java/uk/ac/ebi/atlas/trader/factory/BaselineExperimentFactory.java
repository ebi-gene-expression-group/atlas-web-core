package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperimentConfiguration;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class BaselineExperimentFactory implements ExperimentFactory<BaselineExperiment> {
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;

    public BaselineExperimentFactory(ConfigurationTrader configurationTrader,
                                     SpeciesFactory speciesFactory) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
    }

    @Override
    public BaselineExperiment create(ExperimentDto experimentDto,
                                     ExperimentDesign experimentDesign,
                                     IdfParserOutput idfParserOutput,
                                     List<String> technologyType) {
        checkArgument(
                experimentDto.getExperimentType().isBaseline(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type baseline");

        var configuration = configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());
        var factorsConfig = configurationTrader.getBaselineFactorsConfiguration(experimentDto.getExperimentAccession());
        var alternativeViews = extractAlternativeViews(factorsConfig);

        return new BaselineExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentType().getExpressionType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getSecondaryAccession(),
                idfParserOutput.getTitle(),
                experimentDto.getLoadDate(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                technologyType,
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
                experimentDto.isPrivate(),
                experimentDto.getAccessKey());
    }

    private ImmutablePair<ImmutableList<String>, ImmutableList<String>>
    extractAlternativeViews(BaselineExperimentConfiguration factorsConfig) {
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
