package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.joining;

@Component
@Transactional(transactionManager = "txManager")
public class ExperimentCrudDao {
    private static final String PUBLICATION_SEPARATOR = ", ";

    private static final Function<String, ImmutableSet<String>> PUBLICATION_SPLITTER =
            str ->
                    Arrays.stream(str.split(PUBLICATION_SEPARATOR))
                            .filter(StringUtils::isNotBlank)
                            .map(String::trim)
                            .collect(toImmutableSet());
    private static final Function<Collection<String>, String> PUBLICATION_JOINER =
            publications ->
                    publications.stream()
                            .filter(StringUtils::isNotBlank)
                            .map(String::trim)
                            .collect(joining(PUBLICATION_SEPARATOR));
    private static final Function<Collection<String>, String> PUBLICATION_JOINER_OR_NULL =
            PUBLICATION_JOINER.andThen(str -> str.isBlank() ? null : str);

    private static final RowMapper<ExperimentDto> EXPERIMENT_DTO_ROW_MAPPER =
            (resultSet, __) ->
                    new ExperimentDto(
                        resultSet.getString("accession"),
                        ExperimentType.valueOf(resultSet.getString("type")),
                        resultSet.getString("species"),
                        Optional.ofNullable(resultSet.getString("pubmed_ids"))
                                .map(PUBLICATION_SPLITTER)
                                .orElse(ImmutableSet.of()),
                        Optional.ofNullable(resultSet.getString("dois"))
                                .map(PUBLICATION_SPLITTER)
                                .orElse(ImmutableSet.of()),
                        resultSet.getTimestamp("load_date"),
                        resultSet.getTimestamp("last_update"),
                        resultSet.getBoolean("private"),
                        resultSet.getString("access_key"));

    private final JdbcTemplate jdbcTemplate;

    public ExperimentCrudDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Create
    public void createExperiment(ExperimentDto experimentDto) {
        jdbcTemplate.update(
                "INSERT INTO experiment " +
                "(accession, type, species, private, access_key, pubmed_ids, dois) VALUES (?, ?, ?, ?, ?, ?, ?)",
                experimentDto.getExperimentAccession(),
                experimentDto.getExperimentType().name(),
                experimentDto.getSpecies(),
                experimentDto.isPrivate(),
                experimentDto.getAccessKey(),
                PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getPubmedIds()),
                PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getPubmedIds()));
    }

    // Read
    @Transactional(readOnly = true)
    @Nullable
    public ExperimentDto readExperiment(String experimentAccession) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM experiment WHERE accession=?",
                    EXPERIMENT_DTO_ROW_MAPPER,
                    experimentAccession);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public ImmutableList<ExperimentDto> readExperiments() {
        return ImmutableList.copyOf(
                jdbcTemplate.query(
                        "SELECT * FROM experiment",
                        EXPERIMENT_DTO_ROW_MAPPER));
    }

    // Update
    public void updateExperimentPrivate(String experimentAccession, boolean isPrivate) {
        int updatedRecordsCount =
                jdbcTemplate.update(
                        "UPDATE experiment SET private=? WHERE accession=?",
                        isPrivate,
                        experimentAccession);
        checkState(updatedRecordsCount == 1);
    }

    public void updateExperiment(ExperimentDto experimentDto) {
        int updatedRecordsCount =
                jdbcTemplate.update(
                        "UPDATE experiment SET last_update=NOW(), private=?, pubmed_ids=?, dois=? " +
                        "WHERE accession=?",
                        experimentDto.isPrivate(),
                        PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getPubmedIds()),
                        PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getPubmedIds()),
                        experimentDto.getExperimentAccession());
        checkState(updatedRecordsCount == 1);
    }

    // Delete
    public void deleteExperiment(String experimentAccession) {
        int deletedRecordsCount =
                jdbcTemplate.update(
                        "DELETE FROM experiment WHERE accession=?",
                        experimentAccession);
        checkState(deletedRecordsCount == 1);
    }
}
