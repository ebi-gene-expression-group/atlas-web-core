package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import uk.ac.ebi.atlas.experimentimport.ExperimentDao;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParser;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public abstract class ExperimentTrader {
    protected final ExperimentDao experimentDao;
    protected final ExperimentDesignParser experimentDesignParser;
    protected final IdfParser idfParser;

    public ExperimentTrader(ExperimentDao experimentDao,
                            ExperimentDesignParser experimentDesignParser,
                            IdfParser idfParser) {
        this.experimentDao = experimentDao;
        this.experimentDesignParser = experimentDesignParser;
        this.idfParser = idfParser;
    }

    @NotNull
    @Cacheable(value="experimentByAccession", key="#experimentAccession")
    public abstract Experiment getExperiment(@NotNull String experimentAccession, @NotNull String accessKey);

    @NotNull
    @Cacheable(value="experimentByAccession", key="#experimentAccession")
    public abstract Experiment getExperiment(@NotNull String experimentAccession);

    @NotNull
    @Cacheable(value="experimentByAccession", key="#experimentAccession")
    public Experiment getPublicExperiment(@NotNull String experimentAccession) {
        return getExperiment(experimentAccession, "");
    }

    @NotNull
    @Cacheable("experimentsByType")
    public ImmutableSet<Experiment> getPublicExperiments(@NotNull ExperimentType... experimentTypes) {
        return experimentDao.findPublicExperimentAccessions(experimentTypes)
                .stream()
                .map(accession -> getExperiment(accession, ""))
                .collect(toImmutableSet());
    }
}
