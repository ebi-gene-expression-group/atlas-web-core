package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Repository
public class ExperimentDesignDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ExperimentDesignDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int getTableSize(String experimentAccession) {
        return namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM exp_design INNER JOIN exp_design_column edc on edc.id = exp_design.exp_design_column_id " +
                        "WHERE experiment_accession=:experimentAccession",
                ImmutableMap.of("experimentAccession", experimentAccession),
                Integer.class);
    }

    public Map<String, List<String>> getColumnHeaders(String experimentAccession) {
        return namedParameterJdbcTemplate.query(
                        "SELECT column_name, sample_type " +
                                "FROM exp_design_column WHERE experiment_accession=:experimentAccession " +
                                "ORDER BY id ASC",
                        ImmutableMap.of("experimentAccession", experimentAccession),
                        (ResultSet resultSet) -> {
                            var result = new LinkedHashMap<String, List<String>>();
                            while (resultSet.next()) {
                                var key = resultSet.getString("sample_type");
                                var value = resultSet.getString("column_name");
                                var sampleType = result.getOrDefault(key, new ArrayList<>());
                                sampleType.add(value);
                                result.put(key, sampleType);
                            }
                            return result;
                        });
    }

    private final static String QUERY_FOR_EXPERIMENT_DESIGN_DATA_MICROARRAY =
            "SELECT sample, array_design, annot_value, sample_type " +
                    "FROM exp_design INNER JOIN exp_design_column edc on edc.id = exp_design.exp_design_column_id " +
                    "WHERE experiment_accession = :experimentAccession " +
                    "ORDER BY sample, exp_design_column_id ASC " +
                    "LIMIT :pageSize OFFSET :offset";

    private final static String QUERY_FOR_EXPERIMENT_DESIGN_DATA_NON_MICROARRAY =
            "SELECT sample, annot_value, sample_type " +
                    "FROM exp_design INNER JOIN exp_design_column edc on edc.id = exp_design.exp_design_column_id " +
                    "WHERE experiment_accession = :experimentAccession " +
                    "ORDER BY sample, exp_design_column_id ASC " +
                    "LIMIT :pageSize OFFSET :offset";

    @Value
    public static class ExperimentDesignData {
        LinkedHashMap<String, List<String>> characteristics;
        LinkedHashMap<String, List<String>> factorValues;
        LinkedHashMap<String, List<String>> arrayDesigns;

        public ExperimentDesignData(
                LinkedHashMap<String, List<String>> characteristics,
                LinkedHashMap<String, List<String>> factorValues) {
            this.characteristics = characteristics;
            this.factorValues = factorValues;
            this.arrayDesigns = new LinkedHashMap<>();
        }

        public ExperimentDesignData(
                LinkedHashMap<String, List<String>> characteristics,
                LinkedHashMap<String, List<String>> factorValues,
                LinkedHashMap<String, List<String>> arrayDesigns) {
            this.characteristics = characteristics;
            this.factorValues = factorValues;
            this.arrayDesigns = arrayDesigns;
        }

        public LinkedHashMap<String, List<String>> getArrayDesigns() {
            return arrayDesigns;
        }

        public LinkedHashMap<String, List<String>> getCharacteristics() {
            return characteristics;
        }

        public LinkedHashMap<String, List<String>> getFactorValues() {
            return factorValues;
        }
    }

    public ExperimentDesignData getExperimentDesignData(
            String experimentAccession,
            int pageNo,
            int pageSize) {
        return namedParameterJdbcTemplate.query(
                QUERY_FOR_EXPERIMENT_DESIGN_DATA_NON_MICROARRAY,
                ImmutableMap.of(
                        "experimentAccession", experimentAccession,
                        "pageSize", pageSize,
                        "offset", (pageNo - 1) * pageSize
                ),
                (ResultSet resultSet) -> {
                    var assayToCharacteristics = new LinkedHashMap<String, List<String>>();
                    var assayToFactorValues = new LinkedHashMap<String, List<String>>();

                    while (resultSet.next()) {
                        var sample = resultSet.getString("sample");
                        var annotValue = resultSet.getString("annot_value");
                        var sampleType = resultSet.getString("sample_type");

                        if (sampleType.equalsIgnoreCase("characteristic")) {
                            assayToCharacteristics.computeIfAbsent(sample, k -> new ArrayList<>()).add(annotValue);
                        } else {
                            assayToFactorValues.computeIfAbsent(sample, k -> new ArrayList<>()).add(annotValue);
                        }
                    }

                    return new ExperimentDesignData(assayToCharacteristics, assayToFactorValues);
                }
        );
    }

    public ExperimentDesignData getExperimentDesignDataMicroarray(
            String experimentAccession,
            int pageNo,
            int pageSize) {
        return  namedParameterJdbcTemplate.query(QUERY_FOR_EXPERIMENT_DESIGN_DATA_MICROARRAY,
                ImmutableMap.of("experimentAccession", experimentAccession
                        , "pageSize", pageSize
                        , "offset", (pageNo - 1) * pageSize),
                (ResultSet resultSet) -> {
                    var assayToCharacteristics = new LinkedHashMap<String, List<String>>();
                    var assayToFactorValues = new LinkedHashMap<String, List<String>>();
                    var assayToArrayDesigns = new LinkedHashMap<String, List<String>>();

                    while (resultSet.next()) {
                        var sample = resultSet.getString("sample");
                        var annotValue = resultSet.getString("annot_value");
                        var sampleType = resultSet.getString("sample_type");

                        if (sampleType.equalsIgnoreCase("characteristic")) {
                            assayToCharacteristics.computeIfAbsent(sample, k -> new ArrayList<>()).add(annotValue);
                        } else {
                            assayToFactorValues.computeIfAbsent(sample, k -> new ArrayList<>()).add(annotValue);
                        }
                        var arrayDesign = resultSet.getString("array_design");
                        var sampleAnnotations = assayToArrayDesigns.getOrDefault(sample, new ArrayList<>());
                        sampleAnnotations.add(arrayDesign);
                        assayToArrayDesigns.put(sample, sampleAnnotations);
                    }

                    return new ExperimentDesignData(assayToCharacteristics, assayToFactorValues, assayToArrayDesigns);
                });
    }
}
