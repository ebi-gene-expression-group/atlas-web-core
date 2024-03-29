package uk.ac.ebi.atlas.utils;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import java.util.Collections;
import java.util.Set;

@Component
public class ExperimentSorter {

    private final DataFileHub dataFileHub;
    private final ExperimentTrader experimentTrader;
    private final ConfigurationTrader configurationTrader;

    public ExperimentSorter(DataFileHub dataFileHub,
                            ExperimentTrader experimentTrader,
                            ConfigurationTrader configurationTrader) {
        this.dataFileHub = dataFileHub;
        this.experimentTrader = experimentTrader;
        this.configurationTrader = configurationTrader;
    }

    public TreeMultimap<Long, String> reverseSortAllExperimentsPerSize() {
        return reverseSortExperimentsPerSize(
                ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL,
                ExperimentType.MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL,
                ExperimentType.MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL,
                ExperimentType.RNASEQ_MRNA_DIFFERENTIAL,
                ExperimentType.RNASEQ_MRNA_BASELINE,
                ExperimentType.PROTEOMICS_BASELINE,
                ExperimentType.PROTEOMICS_BASELINE_DIA);
    }

    public TreeMultimap<Long, String> reverseSortExperimentsPerSize(ExperimentType... experimentTypes) {
        TreeMultimap<Long, String> fileSizeToExperimentsMap =
                TreeMultimap.create(Collections.reverseOrder(), Ordering.natural());

        for (ExperimentType experimentType : experimentTypes) {
            for (Experiment experiment: experimentTrader.getPublicExperiments(experimentType)) {
                fileSizeToExperimentsMap.put(
                        estimateSizeOfExperiment(experiment.getAccession(), experimentType),
                        experiment.getAccession());
            }
        }

        return fileSizeToExperimentsMap;
    }

    private long estimateSizeOfExperiment(String experimentAccession, ExperimentType experimentType) {
        switch (experimentType) {
            case RNASEQ_MRNA_BASELINE:
                return estimateSizeOfRnaSeqBaselineExperiment(experimentAccession);
            case PROTEOMICS_BASELINE:
            case PROTEOMICS_BASELINE_DIA:
                return estimateSizeOfProteomicsBaselineExperiment(experimentAccession);
            case RNASEQ_MRNA_DIFFERENTIAL:
                return estimateSizeOfDifferentialExperiment(experimentAccession);
            case MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL:
            case MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL:
                return estimateSizeOfMicroarrayExperiment(experimentAccession);
            default:
                return 0;
        }
    }

    private long estimateSizeOfMicroarrayExperiment(String experimentAccession) {
        Set<String> arrayDesigns =
                configurationTrader
                        .getExperimentConfiguration(experimentAccession).getArrayDesignAccessions();

        long n = 0;
        for (String arrayDesign : arrayDesigns) {
            n += dataFileHub.getMicroarrayExperimentFiles(experimentAccession, arrayDesign).analytics.size();
        }
        return n;
    }

    private long estimateSizeOfDifferentialExperiment(String experimentAccession) {
        return dataFileHub.getBulkDifferentialExperimentFiles(experimentAccession).analytics.size();
    }

    private long estimateSizeOfRnaSeqBaselineExperiment(String experimentAccession) {
        DataFileHub.RnaSeqBaselineExperimentFiles files =
                dataFileHub.getRnaSeqBaselineExperimentFiles(experimentAccession);
        return files.dataFile(files.dataFiles().iterator().next()).size();
    }

    private long estimateSizeOfProteomicsBaselineExperiment(String experimentAccession) {
        return dataFileHub.getProteomicsBaselineExperimentFiles(experimentAccession).main.size();
    }

}
