package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

// I'd prefer to be in experiment design but I need to do
// experiment.propertiesForAssay(runOrAssay)
// One idea: pass in a function to the constructor of experiment design, made from the list of contrasts or assay
// groups, that does this instead
public class ExperimentDesignTable {
    private final Experiment<? extends ReportsGeneExpression> experiment;

    public ExperimentDesignTable(Experiment<? extends ReportsGeneExpression> experiment) {
        this.experiment = experiment;
    }

    public JsonObject asJson() {
        JsonArray headers = threeElementArray(
                headerGroup("", experiment.getExperimentDesign().getAssayHeaders()),
                headerGroup("Sample Characteristics", experiment.getExperimentDesign().getSampleCharacteristicHeaders()),
                headerGroup("Experimental Variables", experiment.getExperimentDesign().getFactorHeaders())
        );

        JsonArray data = new JsonArray();
        for (String runOrAssay: experiment.getExperimentDesign().getAllRunOrAssay()) {
            data.addAll(dataRow(runOrAssay));
        }

        JsonObject result = new JsonObject();
        result.add("headers", headers);
        result.add("data", data);

        return result;
    }


    private JsonObject headerGroup(String name, Collection<String> members) {
        JsonObject result = new JsonObject();
        result.addProperty("name", name);
        result.add("values", GSON.toJsonTree(members));
        return result;
    }

    private JsonArray threeElementArray(JsonElement element1, JsonElement element2, JsonElement element3) {
        JsonArray result = new JsonArray();
        result.add(element1);
        result.add(element2);
        result.add(element3);
        return result;
    }

    private JsonArray dataRow(final String runOrAssay) {
        var jsonArray = new JsonArray();

        // properties will have the analysed column in baseline experiments or ref/test contrast column in differential
        var analysedOrContrastProperties = experiment.propertiesForAssay(runOrAssay);
        for (JsonObject properties : analysedOrContrastProperties) {
            var jsonObject = new JsonObject();
            jsonObject.add("properties", properties);
            jsonObject.add(
                    "values",
                    threeElementArray(
                            GSON.toJsonTree(
                                    isBlank(experiment.getExperimentDesign().getArrayDesign(runOrAssay)) ?
                                            ImmutableList.of(runOrAssay) :
                                            ImmutableList.of(
                                                    runOrAssay,
                                                    experiment.getExperimentDesign().getArrayDesign(runOrAssay))),
                            GSON.toJsonTree(
                                    experiment.getExperimentDesign().getSampleCharacteristicHeaders().stream()
                                            .map(sampleHeader ->
                                                    experiment
                                                            .getExperimentDesign()
                                                            .getSampleCharacteristic(runOrAssay, sampleHeader)
                                                            .getValue())
                                            .collect(toList())),
                            GSON.toJsonTree(
                                    experiment.getExperimentDesign().getFactorHeaders().stream()
                                            .map(factorHeader ->
                                                    experiment
                                                            .getExperimentDesign()
                                                            .getFactorValue(runOrAssay, factorHeader))
                                            .collect(toList()))));

            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }
}
