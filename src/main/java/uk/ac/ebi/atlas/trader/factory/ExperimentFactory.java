package uk.ac.ebi.atlas.trader.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;

public interface ExperimentFactory<E extends Experiment<? extends ReportsGeneExpression>> {
    @NotNull
    E create(@NotNull ExperimentDto experimentDto,
             @NotNull ExperimentDesign experimentDesign,
             @NotNull IdfParserOutput idfParserOutput);
}
