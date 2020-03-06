package uk.ac.ebi.atlas.experiments;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;

import java.text.SimpleDateFormat;

import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

public class ExperimentJsonSerializer {
    public ExperimentJsonSerializer() {
        throw new UnsupportedOperationException();
    }

    private final static String CHAN_ZUCKERBERG_BIOHUB = "Chan-Zuckerberg Biohub";
    private final static String HUMAN_CELL_ATLAS = "Human Cell Atlas";
    private final static String MALARIA_CELL_ATLAS = "Malaria Cell Atlas";

    private final static ImmutableMap<String, ImmutableSet<String>> EXPERIMENT2PROJECT =
        ImmutableMap.<String, ImmutableSet<String>>builder()
                .put("E-ENAD-15", ImmutableSet.of(CHAN_ZUCKERBERG_BIOHUB))
                .put("E-GEOD-81547", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-GEOD-93593", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-MTAB-5061", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-GEOD-106540", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-MTAB-6701", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-MTAB-66782", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-1", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-10", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-11", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-13", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-4", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-5", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-6", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-7", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-8", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-HCAD-9", ImmutableSet.of(HUMAN_CELL_ATLAS))
                .put("E-CURD-2", ImmutableSet.of(MALARIA_CELL_ATLAS))
                .put("E-CURD-3", ImmutableSet.of(MALARIA_CELL_ATLAS))
                .build();

    public static JsonObject serialize(Experiment<?> experiment) {
        switch (experiment.getType()) {
            case SINGLE_CELL_RNASEQ_MRNA_BASELINE:
            case RNASEQ_MRNA_BASELINE:
            case PROTEOMICS_BASELINE:
                return _serializeBaseline(experiment);
            case RNASEQ_MRNA_DIFFERENTIAL:
                return _serializeDifferential((DifferentialExperiment) experiment);
            case MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL:
                return _serializeMicroarray((MicroarrayExperiment) experiment);
            default:
                throw new UnsupportedOperationException("Unsupported experiment type: " + experiment.getType());
        }
    }

    private static JsonObject _serializeBaseline(Experiment<?> experiment) {
        var jsonObject = new JsonObject();

        jsonObject.addProperty("experimentAccession", experiment.getAccession());
        jsonObject.addProperty("experimentDescription", experiment.getDescription());
        jsonObject.addProperty("species", experiment.getSpecies().getName());
        jsonObject.addProperty("kingdom", experiment.getSpecies().getKingdom());
        jsonObject.addProperty("loadDate", new SimpleDateFormat("dd-MM-yyyy").format(experiment.getLoadDate()));
        jsonObject.addProperty("lastUpdate", new SimpleDateFormat("dd-MM-yyyy").format(experiment.getLastUpdate()));
        jsonObject.addProperty("numberOfAssays", experiment.getAnalysedAssays().size());

        jsonObject.addProperty("rawExperimentType", experiment.getType().toString());
        jsonObject.addProperty("experimentType", experiment.getType().isBaseline() ? "Baseline" : "Differential");
        jsonObject.add("technologyType", GSON.toJsonTree(experiment.getTechnologyType()));

        jsonObject.add(
                "experimentalFactors",
                GSON.toJsonTree(experiment.getExperimentDesign().getFactorHeaders()));

        jsonObject.add(
                "experimentProjects",
                GSON.toJsonTree(EXPERIMENT2PROJECT.getOrDefault(experiment.getAccession(), ImmutableSet.of())));

        return jsonObject;
    }

    private static JsonObject _serializeDifferential(DifferentialExperiment experiment) {
        var jsonObject = _serializeBaseline(experiment);

        jsonObject.addProperty("numberOfContrasts", experiment.getDataColumnDescriptors().size());

        return jsonObject;
    }

    private static JsonObject _serializeMicroarray(MicroarrayExperiment experiment) {
        var jsonObject = _serializeDifferential(experiment);

        jsonObject.add(
                "technologyType",
                GSON.toJsonTree(
                        ImmutableSet.<String>builder()
                                .addAll(experiment.getTechnologyType())
                                .addAll(experiment.getArrayDesignNames())
                                .build()));
        jsonObject.add(
                "arrayDesigns",
                GSON.toJsonTree(experiment.getArrayDesignAccessions()));
        jsonObject.add(
                "arrayDesignNames",
                GSON.toJsonTree(experiment.getArrayDesignNames()));

        return jsonObject;
    }
}
