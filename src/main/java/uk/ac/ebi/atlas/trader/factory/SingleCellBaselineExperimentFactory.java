package uk.ac.ebi.atlas.trader.factory;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.singlecell.SingleCellBaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Cell;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.factory.ExperimentFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE;

@Component
public class SingleCellBaselineExperimentFactory implements ExperimentFactory<SingleCellBaselineExperiment> {
    private final SpeciesFactory speciesFactory;

    public SingleCellBaselineExperimentFactory(@NotNull SpeciesFactory speciesFactory) {
        this.speciesFactory = speciesFactory;
    }

    @Override
    @NotNull
    public SingleCellBaselineExperiment create(@NotNull ExperimentDto experimentDto,
                                               @NotNull ExperimentDesign experimentDesign,
                                               @NotNull IdfParserOutput idfParserOutput) {
        checkArgument(
                experimentDto.getExperimentType().isSingleCell(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type single cell");

        return new SingleCellBaselineExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getTitle(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                experimentDesign.getAllRunOrAssay().stream().map(Cell::new).collect(toList()),
                experimentDesign,
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                "");
    }
}
