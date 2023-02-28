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
                                "ORDER BY column_order ASC",
                        ImmutableMap.of("experimentAccession", experimentAccession),
                        (ResultSet resultSet) -> {
                            var result = new LinkedHashMap<String, List<String>>();
                            while (resultSet.next()) {
                                var key = resultSet.getString("sample_type");
                                var value = resultSet.getString("column_name");
                                if (result.containsKey(key))
                                    result.get(key).add(value);
                                else {
                                    result.put(key, new ArrayList<>(List.of(value)));
                                }
                            }
                            return result;
                        });
    }

    public ImmutableList<LinkedHashMap<String, List<String>>> getExperimentDesignData(
            String experimentAccession,
            boolean isMicroArrayExperiment,
            int pageNo,
            int pageSize) {
        return  namedParameterJdbcTemplate.query(
                "SELECT sample, array_design, annot_value, sample_type " +
                        "FROM exp_design INNER JOIN exp_design_column edc on edc.id = exp_design.exp_design_column_id " +
                        "WHERE experiment_accession = :experimentAccession " +
                        "LIMIT :pageSize OFFSET :offset",
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
                          if(assayToCharacteristics.containsKey(sample)) {
                            assayToCharacteristics.get(sample).add(annot_value);
                          } else {
                            assayToCharacteristics.put(sample, new ArrayList<>(List.of(annot_value)));
                          }
                        } else {
                            if(assayToFactorValues.containsKey(sample)) {
                                assayToFactorValues.get(sample).add(annot_value);
                            } else {
                                assayToFactorValues.put(sample, new ArrayList<>(List.of(annot_value)));
                            }
                        }
                        // for microarray experiments array_design column will be populated
                        // which is not the case for other types of experiments in which case
                        // its value would be null so we can ignore it.
                        if (isMicroArrayExperiment) {
                            var array_design = resultSet.getString("array_design");
                            //arrayDesigns.add(array_design);
                            if(assayToArrayDesigns.containsKey(sample)) {
                                assayToArrayDesigns.get(sample).add(array_design);
                            } else {
                                assayToArrayDesigns.put(sample, new ArrayList<>(List.of(array_design)));
                            }
                        }
                    }
                    result.add(assayToCharacteristics);
                    result.add(assayToFactorValues);
                    if(isMicroArrayExperiment) {
                        result.add(assayToArrayDesigns);
                    }
                    return ImmutableList.copyOf(result);
                });
    }
}
