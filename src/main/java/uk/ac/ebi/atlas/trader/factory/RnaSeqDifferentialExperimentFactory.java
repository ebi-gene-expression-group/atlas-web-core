package uk.ac.ebi.atlas.trader.factory;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.trader.factory.ExperimentFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_DIFFERENTIAL;

@Component
public class RnaSeqDifferentialExperimentFactory implements ExperimentFactory<DifferentialExperiment> {
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;

    public RnaSeqDifferentialExperimentFactory(@NotNull ConfigurationTrader configurationTrader,
                                               @NotNull SpeciesFactory speciesFactory) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
    }

    @Override
    @NotNull
    public DifferentialExperiment create(@NotNull ExperimentDto experimentDto,
                                         @NotNull ExperimentDesign experimentDesign,
                                         @NotNull IdfParserOutput idfParserOutput) {
        checkArgument(
                experimentDto.getExperimentType().isRnaSeqDifferential(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type RNA-seq differential");

        ExperimentConfiguration experimentConfiguration =
                configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());

        return new DifferentialExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getTitle(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                experimentConfiguration.getContrastAndAnnotationPairs(),
                experimentDesign,
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                experimentDto.isPrivate());
    }
}
