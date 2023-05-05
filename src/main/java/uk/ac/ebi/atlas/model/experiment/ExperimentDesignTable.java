package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.trader.ExperimentDesignDao;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

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

    private LinkedHashMap<String, List<String>> assayToFactorValues = new LinkedHashMap<>();
    private LinkedHashMap<String, List<String>> assayToCharacteristicValues = new LinkedHashMap<>();
    private LinkedHashMap<String, List<String>> assayToArrayDesigns = new LinkedHashMap<>();

    public ExperimentDesignTable(Experiment<? extends ReportsGeneExpression> experiment,
                                 ExperimentDesignDao experimentDesignDao) {
        this.experiment = experiment;
        this.experimentDesignDao = experimentDesignDao;
    }

    public JsonObject asJson(String experiment_accession, int pageNo, int pageSize) {
        var columnHeaders = experimentDesignDao.getColumnHeaders(experiment_accession);
        var experiment_type = experiment.getType();

        JsonArray headers = threeElementArray(
                headerGroup("", getAssayHeaders(experiment_type)),
                headerGroup("Sample Characteristics", columnHeaders.get(CHARACTERISTIC_COLUMN)),
                headerGroup("Experimental Variables", columnHeaders.get(FACTOR_COLUMN))
        );

        pageSize *= columnHeaders.get(CHARACTERISTIC_COLUMN).size() + columnHeaders.get(FACTOR_COLUMN).size();

        var expDesignData = experiment_type.isMicroarray() ?
                experimentDesignDao.getExperimentDesignDataMicroarray(experiment_accession, pageNo, pageSize) :
                experimentDesignDao.getExperimentDesignData(experiment_accession, pageNo, pageSize);

        assayToCharacteristicValues = expDesignData.get(0);
        assayToFactorValues = expDesignData.get(1);
        if (experiment_type.isMicroarray())
            assayToArrayDesigns = expDesignData.get(2);

        JsonArray data = new JsonArray();
        // The number of assays is the same for all factors and characteristics so we can use any of them
        assayToCharacteristicValues.keySet().forEach(
                runOrAssay -> data.add(dataRow(
                        runOrAssay,
                        columnHeaders.get(CHARACTERISTIC_COLUMN),
                        columnHeaders.get(FACTOR_COLUMN),
                        experiment_type.isMicroarray()).get(0))
        );

        JsonObject result = new JsonObject();
        result.add("headers", headers);
        result.add("data", data);
        result.add("totalNoOfRows", GSON.toJsonTree(experimentDesignDao.getTableSize(experiment_accession)));

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
            GSON.toJsonTree(data);
        }
        return jsonArray;
    }

    private List<String> getAssayHeaders(ExperimentType type) {
        switch (type) {
            case MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL:
                return Lists.newArrayList("Assay", "Array");
            case RNASEQ_MRNA_BASELINE:
            case RNASEQ_MRNA_DIFFERENTIAL:
            case PROTEOMICS_BASELINE:
            case PROTEOMICS_DIFFERENTIAL:
            case PROTEOMICS_BASELINE_DIA:
                return Lists.newArrayList("Run");
            case SINGLE_CELL_RNASEQ_MRNA_BASELINE:
            case SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE:
                return Lists.newArrayList("Assay");
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }
}
