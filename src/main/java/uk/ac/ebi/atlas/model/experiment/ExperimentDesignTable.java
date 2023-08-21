package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.trader.ExperimentDesignDao;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

// I'd prefer to be in experiment design but I need to do
// experiment.propertiesForAssay(runOrAssay)
// One idea: pass in a function to the constructor of experiment design, made from the list of contrasts or assay
// groups, that does this instead
public class ExperimentDesignTable {
    public static final String CHARACTERISTIC_COLUMN = "characteristic";
    public static final String FACTOR_COLUMN = "factor";
    private final Experiment<? extends ReportsGeneExpression> experiment;
    private final ExperimentDesignDao experimentDesignDao;

    private Map<String, List<String>> assayToFactorValues = new LinkedHashMap<>();
    private Map<String, List<String>> assayToCharacteristicValues = new LinkedHashMap<>();
    private Map<String, List<String>> assayToArrayDesigns = new LinkedHashMap<>();

    public ExperimentDesignTable(Experiment<? extends ReportsGeneExpression> experiment,
                                 ExperimentDesignDao experimentDesignDao) {
        this.experiment = experiment;
        this.experimentDesignDao = experimentDesignDao;
    }

    public JsonObject asJson(String experimentAccession, int pageNo, int pageSize) {
        var columnHeaders = experimentDesignDao.getColumnHeaders(experimentAccession);
        var experimentType = experiment.getType();

        JsonArray headers = experimentDesignArrays(
                headerGroup("", experimentType.getAssayHeaders()),
                headerGroup("Sample Characteristics", columnHeaders.get(CHARACTERISTIC_COLUMN)),
                headerGroup("Experimental Variables", columnHeaders.get(FACTOR_COLUMN))
        );

        pageSize *= columnHeaders.get(CHARACTERISTIC_COLUMN).size() + columnHeaders.get(FACTOR_COLUMN).size();

        var expDesignData = experimentType.isMicroarray() ?
                experimentDesignDao.getExperimentDesignDataMicroarray(experimentAccession, pageNo, pageSize) :
                experimentDesignDao.getExperimentDesignData(experimentAccession, pageNo, pageSize);

        assayToCharacteristicValues = expDesignData.getCharacteristics();
        assayToFactorValues = expDesignData.getFactorValues();
        if (experimentType.isMicroarray())
            assayToArrayDesigns = expDesignData.getArrayDesigns();

        JsonArray data = new JsonArray();
        // The number of assays is the same for all factors and characteristics so we can use any of them
        assayToCharacteristicValues.keySet().forEach(
                runOrAssay -> data.add(dataRow(
                        runOrAssay,
                        columnHeaders.get(CHARACTERISTIC_COLUMN),
                        columnHeaders.get(FACTOR_COLUMN),
                        experimentType.isMicroarray()).get(0))
        );

        JsonObject result = new JsonObject();
        result.add("headers", headers);
        result.add("data", data);
        result.add("totalNoOfRows", GSON.toJsonTree(experimentDesignDao.getTableSize(experimentAccession)));

        return result;
    }

    private JsonObject headerGroup(String name, Collection<String> members) {
        JsonObject result = new JsonObject();
        result.addProperty("name", name);
        result.add("values", GSON.toJsonTree(members));
        return result;
    }

    private JsonArray experimentDesignArrays(JsonElement element1, JsonElement element2, JsonElement element3) {
        JsonArray result = new JsonArray();
        result.add(element1);
        result.add(element2);
        result.add(element3);
        return result;
    }

    private JsonArray dataRow(final String runOrAssay, List<String> sampleHeaders, List<String> experimentalHeaders,
                               boolean isMicroarrayExperiment) {
        var jsonArray = new JsonArray();

        // properties will have the analysed column in baseline experiments or ref/test contrast column in differential
        var analysedOrContrastProperties = experiment.propertiesForAssay(runOrAssay);
        for (JsonObject properties : analysedOrContrastProperties) {
            LinkedHashMap<String, String> data = new LinkedHashMap<>();

            for (String propertyKey : properties.keySet()) {
                data.put(propertyKey, properties.get(propertyKey).toString());
            }

            data.put("run", !isMicroarrayExperiment ?
                    runOrAssay :
                    ImmutableList.of(
                            runOrAssay,
                            assayToArrayDesigns.get(runOrAssay)).toString());
            if (assayToCharacteristicValues.get(runOrAssay) != null) {
                for (int i = 0; i < Math.min(sampleHeaders.size(), assayToCharacteristicValues.get(runOrAssay).size()); i++) {
                    data.put(sampleHeaders.get(i), assayToCharacteristicValues.get(runOrAssay).get(i));
                }
            }
            if (assayToFactorValues.get(runOrAssay) != null) {
                for (int i = 0; i < Math.min(experimentalHeaders.size(), assayToFactorValues.get(runOrAssay).size()); i++) {
                    data.put(experimentalHeaders.get(i), assayToFactorValues.get(runOrAssay).get(i));
                }
            }
            jsonArray.add(GSON.toJsonTree(data));
        }
        return jsonArray;
    }

}
