package uk.ac.ebi.atlas.experimentimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.experimentimport.condensedSdrf.CondensedSdrfParser;
import uk.ac.ebi.atlas.experimentimport.experimentdesign.ExperimentDesignFileWriterService;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParser;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

// Inserts experiment in (scxa_)experiment table and writes the experiment design file to expdesign/
public abstract class ExperimentCrud {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentCrud.class);

    protected final ExperimentDao experimentDao;
    protected final ExperimentChecker experimentChecker;
    protected final ExperimentDesignFileWriterService experimentDesignFileWriterService;
    protected final CondensedSdrfParser condensedSdrfParser;
    protected final IdfParser idfParser;

    public ExperimentCrud(ExperimentDao experimentDao,
                          ExperimentChecker experimentChecker,
                          CondensedSdrfParser condensedSdrfParser,
                          IdfParser idfParser,
                          ExperimentDesignFileWriterService experimentDesignFileWriterService) {

        this.experimentDao = experimentDao;
        this.experimentChecker = experimentChecker;
        this.condensedSdrfParser = condensedSdrfParser;
        this.idfParser = idfParser;
        this.experimentDesignFileWriterService = experimentDesignFileWriterService;
    }

    public abstract UUID importExperiment(String experimentAccession, boolean isPrivate);
    public abstract void updateExperimentDesign(String experimentAccession);

    public ExperimentDto findExperiment(String experimentAccession) {
        return experimentDao.getExperimentAsAdmin(experimentAccession);
    }

    public List<ExperimentDto> findAllExperiments() {
        return experimentDao.getAllExperimentsAsAdmin();
    }

    public void makeExperimentPrivate(String experimentAccession) {
        setExperimentPrivacyStatus(experimentAccession, true);
    }

    public void makeExperimentPublic(String experimentAccession) {
        setExperimentPrivacyStatus(experimentAccession, false);
    }

    public void deleteExperiment(String experimentAccession) {
        var experimentDTO = findExperiment(experimentAccession);
        checkNotNull(experimentDTO, MessageFormat.format("Experiment not found: {0}", experimentAccession));
        experimentDao.deleteExperiment(experimentDTO.getExperimentAccession());
    }

    private void setExperimentPrivacyStatus(String experimentAccession, boolean newPrivacyStatus) {
        experimentDao.setExperimentPrivacyStatus(experimentAccession, newPrivacyStatus);
        checkState(
                newPrivacyStatus == experimentDao.getExperimentAsAdmin(experimentAccession).isPrivate(),
                "Failed to change is_private column in the DB! (?)");
    }

    protected Optional<String> fetchExperimentAccessKey(String experimentAccession) {
        try {
            var experiment = findExperiment(experimentAccession);
            return Optional.of(experiment.getAccessKey());
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    protected void updateWithNewExperimentDesign(ExperimentDesign newDesign, ExperimentDto experimentDTO) {
        try {
            experimentDesignFileWriterService.writeExperimentDesignFile(
                    experimentDTO.getExperimentAccession(),
                    experimentDTO.getExperimentType(),
                    newDesign);
            LOGGER.info("updated design for experiment {}", experimentDTO.getExperimentAccession());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
