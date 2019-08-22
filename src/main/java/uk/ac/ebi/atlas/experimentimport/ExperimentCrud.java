package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableList;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import uk.ac.ebi.atlas.experimentimport.experimentdesign.ExperimentDesignFileWriterService;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.UUID;

// IMPORTANT: If your client application should evict from caches other than the ones here, remember to add a wrapping
// method (e.g. t-SNE stuff in SC). If this is turned into an interface (which, in all fairness, might be the best
// choice), all the caching annotations would be moved to the implementing classes and this note should be removed.
public abstract class ExperimentCrud {
    protected final ExperimentCrudDao experimentCrudDao;
    private final ExperimentDesignFileWriterService experimentDesignFileWriterService;

    public ExperimentCrud(ExperimentCrudDao experimentCrudDao,
                          ExperimentDesignFileWriterService experimentDesignFileWriterService) {
        this.experimentCrudDao = experimentCrudDao;
        this.experimentDesignFileWriterService = experimentDesignFileWriterService;
    }

    // Create
    @Caching(evict = {
            @CacheEvict(cacheNames = "experiment", key = "#experimentAccession"),
            @CacheEvict(cacheNames = "experimentAttributes", key = "#experimentAccession") })
    public abstract UUID createExperiment(String experimentAccession, boolean isPrivate);

    // Read
    public Optional<ExperimentDto> readExperiment(String experimentAccession) {
        return Optional.ofNullable(experimentCrudDao.readExperiment(experimentAccession));
    }

    public ImmutableList<ExperimentDto> readExperiments() {
        return experimentCrudDao.readExperiments();
    }

    // Update
    @Caching(evict = {
            @CacheEvict(cacheNames = "experiment", key = "#experimentAccession"),
            @CacheEvict(cacheNames = "experimentAttributes", key = "#experimentAccession") })
    public void updateExperimentPrivate(String experimentAccession, boolean isPrivate) {
        experimentCrudDao.updateExperimentPrivate(experimentAccession, isPrivate);
    }

    // Delete
    @Caching(evict = {
            @CacheEvict(cacheNames = "experiment", key = "#experimentAccession"),
            @CacheEvict(cacheNames = "experimentAttributes", key = "#experimentAccession") })
    public void deleteExperiment(String experimentAccession) {
        experimentCrudDao.deleteExperiment(experimentAccession);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "experiment", key = "#experimentAccession"),
            @CacheEvict(cacheNames = "experimentAttributes", key = "#experimentAccession") })
    public abstract void updateExperimentDesign(String experimentAccession);

    protected void updateExperimentDesign(ExperimentDesign experimentDesign, ExperimentDto experimentDto) {
        try {
            experimentDesignFileWriterService.writeExperimentDesignFile(
                    experimentDto.getExperimentAccession(),
                    experimentDto.getExperimentType(),
                    experimentDesign);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
