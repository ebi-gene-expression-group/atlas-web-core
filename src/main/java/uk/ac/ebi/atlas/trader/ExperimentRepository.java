package uk.ac.ebi.atlas.trader;

import uk.ac.ebi.atlas.model.experiment.Experiment;

public interface ExperimentRepository {

    Experiment getExperiment(String experimentAccession);

    String getExperimentType(String experimentAccession);
}
