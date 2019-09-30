package uk.ac.ebi.atlas.trader.factory;

import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.experimentimport.sdrf.SdrfParser;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;

public interface ExperimentFactory<E extends Experiment<? extends ReportsGeneExpression>> {
    E create(ExperimentDto experimentDto,
             ExperimentDesign experimentDesign,
             IdfParserOutput idfParserOutput,
             SdrfParser sdrfParser);
}
