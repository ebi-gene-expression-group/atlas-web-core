package uk.ac.ebi.atlas.trader.factory;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.sdrf.SdrfParser;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesignDao;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class MicroarrayExperimentFactory implements ExperimentFactory<MicroarrayExperiment> {
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;
    private final ArrayDesignDao arrayDesignDao;

    public MicroarrayExperimentFactory(ConfigurationTrader configurationTrader,
                                       SpeciesFactory speciesFactory,
                                       ArrayDesignDao arrayDesignDao) {

        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
        this.arrayDesignDao = arrayDesignDao;
    }

    @Override
    public MicroarrayExperiment create(ExperimentDto experimentDto,
                                       ExperimentDesign experimentDesign,
                                       IdfParserOutput idfParserOutput,
                                       SdrfParser sdrfParser) {
        checkArgument(
                experimentDto.getExperimentType().isMicroarray(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type microarray");

        ExperimentConfiguration experimentConfiguration =
                configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());

        return new MicroarrayExperiment(
                sdrfParser.parse(experimentDto.getExperimentAccession()),
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
                experimentConfiguration
                        .getArrayDesignAccessions()
                        .stream()
                        .map(arrayDesignDao::getArrayDesign)
                        .collect(Collectors.toList()),
                experimentDto.isPrivate(),
                experimentDto.getAccessKey());
    }
}
