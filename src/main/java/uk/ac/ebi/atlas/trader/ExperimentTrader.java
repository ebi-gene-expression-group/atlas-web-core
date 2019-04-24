package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDao;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParser;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

@NonNullByDefault
@Component
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

    protected abstract Experiment buildExperiment(ExperimentDto experimentDto);

    @Cacheable(value="experimentByAccession")
    public Experiment getExperiment(String experimentAccession, String accessKey) {
        return buildExperiment(experimentDao.findExperiment(experimentAccession, accessKey));
    }

    public Experiment getExperiment(String experimentAccession) {
        return buildExperiment(experimentDao.getExperimentAsAdmin(experimentAccession));
    }

    @Cacheable(value="experimentByAccession")
    public Experiment getPublicExperiment(String experimentAccession) {
        return getExperiment(experimentAccession, "");
    }

    @Cacheable("experimentsByType")
    public ImmutableSet<Experiment> getPublicExperiments(ExperimentType... experimentTypes) {
        return experimentDao.findPublicExperimentAccessions(experimentTypes)
                .stream()
                .map(accession -> getExperiment(accession, ""))
                .collect(toImmutableSet());
    }
}
