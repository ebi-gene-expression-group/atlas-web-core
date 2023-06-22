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

    public ImmutableList<LinkedHashMap<String, List<String>>> getExperimentDesignData(
            String experimentAccession,
            int pageNo,
            int pageSize) {
        return  namedParameterJdbcTemplate.query(
                QUERY_FOR_EXPERIMENT_DESIGN_DATA_NON_MICROARRAY,
                ImmutableMap.of("experimentAccession", experimentAccession
                        , "pageSize", pageSize
                        , "offset", (pageNo - 1) * pageSize),
                (ResultSet resultSet) -> {
                    var result = new ArrayList<LinkedHashMap<String, List<String>>>();
                    var assayToCharacteristics = new LinkedHashMap<String, List<String>>();
                    var assayToFactorValues = new LinkedHashMap<String, List<String>>();

                    while (resultSet.next()) {
                        var sample = resultSet.getString("sample");
                        var annot_value = resultSet.getString("annot_value");
                        var sample_type = resultSet.getString("sample_type");

                        if(sample_type.equalsIgnoreCase("characteristic")) {
                          var sampleAnnotations = assayToCharacteristics.getOrDefault(sample, new ArrayList<>());
                          sampleAnnotations.add(annotValue);
                          assayToCharacteristics.put(sample, sampleAnnotations);
                        } else {
                          var sampleAnnotations = assayToFactorValues.getOrDefault(sample, new ArrayList<>());
                          sampleAnnotations.add(annotValue);
                          assayToFactorValues.put(sample, sampleAnnotations);
                        }
                    }
                    result.add(assayToCharacteristics);
                    result.add(assayToFactorValues);

                    return ImmutableList.copyOf(result);
                });
    }

    public ImmutableList<LinkedHashMap<String, List<String>>> getExperimentDesignDataMicroarray(
            String experimentAccession,
            int pageNo,
            int pageSize) {
        return  namedParameterJdbcTemplate.query(QUERY_FOR_EXPERIMENT_DESIGN_DATA_MICROARRAY,
                ImmutableMap.of("experimentAccession", experimentAccession
                        , "pageSize", pageSize
                        , "offset", (pageNo - 1) * pageSize),
                (ResultSet resultSet) -> {
                    var result = new ArrayList<LinkedHashMap<String, List<String>>>();
                    var assayToCharacteristics = new LinkedHashMap<String, List<String>>();
                    var assayToFactorValues = new LinkedHashMap<String, List<String>>();
                    var assayToArrayDesigns = new LinkedHashMap<String, List<String>>();

                    while (resultSet.next()) {
                        var sample = resultSet.getString("sample");
                        var annot_value = resultSet.getString("annot_value");
                        var sample_type = resultSet.getString("sample_type");

                        if(sample_type.equalsIgnoreCase("characteristic")) {
                          var sampleAnnotations = assayToCharacteristics.getOrDefault(sample, new ArrayList<>());
                          sampleAnnotations.add(annotValue);
                          assayToCharacteristics.put(sample, sampleAnnotations);
                        } else {
                          var sampleAnnotations = assayToFactorValues.getOrDefault(sample, new ArrayList<>());
                          sampleAnnotations.add(annotValue);
                          assayToFactorValues.put(sample, sampleAnnotations);
                        }
                        var array_design = resultSet.getString("array_design");
                        var sampleAnnotations = assayToArrayDesigns.getOrDefault(sample, new ArrayList<>());
                        sampleAnnotations.add(arrayDesign);
                        assayToArrayDesigns.put(sample, sampleAnnotations);
                    }
                    result.add(assayToCharacteristics);
                    result.add(assayToFactorValues);
                    result.add(assayToArrayDesigns);

                    return ImmutableList.copyOf(result);
                });
    }
}
