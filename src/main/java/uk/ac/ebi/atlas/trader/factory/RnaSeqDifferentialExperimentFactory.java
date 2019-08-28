package uk.ac.ebi.atlas.trader.factory;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class RnaSeqDifferentialExperimentFactory implements ExperimentFactory<DifferentialExperiment> {
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;

    public RnaSeqDifferentialExperimentFactory(ConfigurationTrader configurationTrader,
                                               SpeciesFactory speciesFactory) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
    }

    @Override
    public DifferentialExperiment create(ExperimentDto experimentDto,
                                         ExperimentDesign experimentDesign,
                                         IdfParserOutput idfParserOutput) {
        checkArgument(
                experimentDto.getExperimentType().isRnaSeqDifferential(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type RNA-seq differential");

        ExperimentConfiguration experimentConfiguration =
                configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());

        return new DifferentialExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getTitle(),
                experimentDto.getLoadDate(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                experimentConfiguration.getContrastAndAnnotationPairs(),
                experimentDesign,
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                experimentDto.isPrivate(),
                experimentDto.getAccessKey());
    }
}
