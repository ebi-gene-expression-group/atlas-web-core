package uk.ac.ebi.atlas.experiments;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experiments.collections.ExperimentCollection;
import uk.ac.ebi.atlas.experiments.collections.ExperimentCollectionsFinderService;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;

import java.text.SimpleDateFormat;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@Component
public class ExperimentJsonSerializer {
    private final ExperimentCollectionsFinderService experimentCollectionsService;

    public ExperimentJsonSerializer(ExperimentCollectionsFinderService experimentCollectionsService) {
        this.experimentCollectionsService = experimentCollectionsService;
    }

    public JsonObject serialize(Experiment<?> experiment) {
        switch (experiment.getType()) {
            case SINGLE_CELL_RNASEQ_MRNA_BASELINE:
            case RNASEQ_MRNA_BASELINE:
            case PROTEOMICS_BASELINE:
            case PROTEOMICS_BASELINE_DIA_SWATH:
                return _serializeBaseline(experiment);
            case RNASEQ_MRNA_DIFFERENTIAL:
            case PROTEOMICS_DIFFERENTIAL:
                return _serializeDifferential((DifferentialExperiment) experiment);
            case MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL:
                return _serializeMicroarray((MicroarrayExperiment) experiment);
            default:
                throw new UnsupportedOperationException("Unsupported experiment type: " + experiment.getType());
        }
    }

    private JsonObject _serializeBaseline(Experiment<?> experiment) {
        var jsonObject = new JsonObject();

        jsonObject.addProperty("experimentAccession", experiment.getAccession());
        jsonObject.addProperty("experimentDescription", experiment.getDescription());
        jsonObject.addProperty("species", experiment.getSpecies().getName());
        jsonObject.addProperty("kingdom", experiment.getSpecies().getKingdom());
        jsonObject.addProperty(
                "loadDate", new SimpleDateFormat("dd-MM-yyyy")
                        .format(experiment.getLoadDate()));
        jsonObject.addProperty(
                "lastUpdate", new SimpleDateFormat("dd-MM-yyyy")
                        .format(experiment.getLastUpdate()));
        jsonObject.addProperty(
                "numberOfAssays", experiment.getAnalysedAssays().size());
        jsonObject.addProperty(
                "rawExperimentType", experiment.getType().toString());
        jsonObject.addProperty(
                "experimentType", experiment.getType().isBaseline() ? "Baseline" : "Differential");
        jsonObject.add("technologyType", GSON.toJsonTree(experiment.getTechnologyType()));
        jsonObject.add(
                "experimentalFactors",
                GSON.toJsonTree(experiment.getExperimentDesign().getFactorHeaders()));
        jsonObject.add(
                "experimentProjects",
                GSON.toJsonTree(
                        experimentCollectionsService
                                .getExperimentCollections(experiment.getAccession())
                                .stream()
                                .map(ExperimentCollection::name)
                                .collect(toImmutableSet())));
        return jsonObject;
    }

    private JsonObject _serializeDifferential(DifferentialExperiment experiment) {
        var jsonObject = _serializeBaseline(experiment);

        jsonObject.addProperty("numberOfContrasts", experiment.getDataColumnDescriptors().size());

        return jsonObject;
    }

    private JsonObject _serializeMicroarray(MicroarrayExperiment experiment) {
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
