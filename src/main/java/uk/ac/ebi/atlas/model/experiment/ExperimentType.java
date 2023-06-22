package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.Lists;

import java.util.*;

public enum ExperimentType {
    RNASEQ_MRNA_BASELINE("rnaseq_mrna_baseline", "RNA-Seq mRNA baseline", Lists.newArrayList("Run")),
    RNASEQ_MRNA_DIFFERENTIAL("rnaseq_mrna_differential", "RNA-Seq mRNA differential", Lists.newArrayList("Run")),
    MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL("microarray_1colour_mrna_differential", "Microarray 1-colour mRNA", Lists.newArrayList("Assay", "Array")),
    MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL("microarray_2colour_mrna_differential", "Microarray 2-colour mRNA", Lists.newArrayList("Assay", "Array")),
    MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL("microarray_1colour_microrna_differential", "Microarray 1-colour miRNA", Lists.newArrayList("Assay", "Array")),
    PROTEOMICS_BASELINE("proteomics_baseline", "Proteomics baseline", Lists.newArrayList("Run")),
    SINGLE_CELL_RNASEQ_MRNA_BASELINE("scrnaseq_mrna_baseline", "Single-cell RNA-Seq mRNA baseline", Lists.newArrayList("Assay")),
    PROTEOMICS_DIFFERENTIAL("proteomics_differential", "Proteomics differential", Lists.newArrayList("Run")),
    PROTEOMICS_BASELINE_DIA("proteomics_baseline_dia", "Proteomics baseline DIA", Lists.newArrayList("Run")),
    SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE("snrnaseq_mrna_baseline", "Single-nucleus RNA-Seq mRNA baseline", Lists.newArrayList("Assay"));

    private String description;
    private String humanDescription;
    private List<String> assayHeaders;

    ExperimentType(String description, String humanDescription, List<String> assayHeaders) {
        this.description = description;
        this.humanDescription = humanDescription;
        this.assayHeaders = assayHeaders;
    }

    public boolean isSingleCell() {
        return equals(SINGLE_CELL_RNASEQ_MRNA_BASELINE) ||
               equals(SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE);
    }

    public boolean isMicroarray() {
        return equals(MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL) ||
               equals(MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL) ||
               equals(MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL);
    }

    public boolean isBaseline() {
        return equals(RNASEQ_MRNA_BASELINE) ||
               equals(PROTEOMICS_BASELINE) ||
               equals(SINGLE_CELL_RNASEQ_MRNA_BASELINE) ||
               equals(PROTEOMICS_BASELINE_DIA) ||
               equals(SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE);
    }

    public boolean isProteomicsBaseline() {
        return equals(PROTEOMICS_BASELINE)|| equals(PROTEOMICS_BASELINE_DIA);
    }

    public boolean isProteomicsDifferential() {
        return equals(PROTEOMICS_DIFFERENTIAL);
    }

    public boolean isRnaSeqDifferential() {
        return equals(RNASEQ_MRNA_DIFFERENTIAL);
    }

    public boolean isBulkDifferential() {
        return equals(RNASEQ_MRNA_DIFFERENTIAL) || equals(PROTEOMICS_DIFFERENTIAL);
    }

    public boolean isDifferential() {
        return equals(RNASEQ_MRNA_DIFFERENTIAL) || equals(PROTEOMICS_DIFFERENTIAL) || isMicroarray();
    }

    public boolean isMicroRna() {
        return equals(MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL);
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAssayHeaders() {
        return assayHeaders;
    }

    private static final Map<String, ExperimentType> TYPE_BY_DESCRIPTION = new HashMap<>();

    static {
        for (ExperimentType experimentType : EnumSet.allOf(ExperimentType.class)) {
            TYPE_BY_DESCRIPTION.put(experimentType.getDescription(), experimentType);
        }
    }

    public static ExperimentType get(String experimentTypeDescription) {
        return TYPE_BY_DESCRIPTION.get(experimentTypeDescription.toLowerCase());
    }

    public static boolean containsBaseline(Set<String> experimentTypes) {
        return experimentTypes.contains(RNASEQ_MRNA_BASELINE.name()) ||
               experimentTypes.contains(PROTEOMICS_BASELINE.name()) ||
               experimentTypes.contains(PROTEOMICS_BASELINE_DIA.name());
    }

    public static boolean containsDifferential(Set<String> experimentTypes) {
        return experimentTypes.contains(
                RNASEQ_MRNA_DIFFERENTIAL.name()) ||
                experimentTypes.contains(PROTEOMICS_DIFFERENTIAL.name()) ||
                experimentTypes.contains(MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL.name()) ||
                experimentTypes.contains(MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL.name()) ||
                experimentTypes.contains(MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL.name());
    }

    public boolean isRnaSeqBaseline() {
        return equals(RNASEQ_MRNA_BASELINE);
    }

    // Used by EL in experiment-description.jsp
    public String getHumanDescription() {
        return humanDescription;
    }
}
