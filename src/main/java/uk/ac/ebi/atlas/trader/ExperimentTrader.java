package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

@Component
public interface ExperimentTrader {
    // Under most circumstances you should use getExperiment(experimentAccession, accessKey). This method will return
    // any experiment, public or private, disregarding the private flag and without requiring the access key.
    // Use with care!
    Experiment getExperimentForAnalyticsIndex(String experimentAccession);

    Experiment getPublicExperiment(String experimentAccession);
    Experiment getExperiment(String experimentAccession, String accessKey);
    ImmutableSet<Experiment> getPublicExperiments(ExperimentType... types);
}