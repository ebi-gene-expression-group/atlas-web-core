package uk.ac.ebi.atlas.testutils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class JdbcUtils {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcUtils(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<String> fetchAllExperimentAccessions() {
        return jdbcTemplate.queryForList("SELECT accession FROM experiment", String.class);
    }

    public String fetchRandomExperimentAccession() {
        return jdbcTemplate.queryForObject(
                "SELECT accession FROM experiment ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomExperimentAccession(ExperimentType... experimentTypes) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource(
                        "experiment_types",
                        Arrays.stream(experimentTypes).map(ExperimentType::toString).collect(toImmutableList()));

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT accession FROM experiment WHERE type IN (:experiment_types) AND private=FALSE ORDER BY RANDOM() LIMIT 1",
                namedParameters,
                String.class);
    }

    public List<String> fetchPublicExperimentAccessions() {
        return jdbcTemplate.queryForList("SELECT accession FROM experiment WHERE private=FALSE", String.class);
    }

    public String fetchRandomPublicExperimentAccession() {
        return jdbcTemplate.queryForObject(
                "SELECT accession FROM experiment WHERE private=FALSE ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomSingleCellExperimentAccessionWithoutMarkerGenes() {
        // This is a bit of a naive approach that only really works with mock data. In reality, all experiments seen so
        // far have at least one marker gene for some k value. A better function would return a pair of (experiment
        // accession, k) that don't have marker genes.
        return jdbcTemplate.queryForObject(
                "SELECT cell_group_membership.experiment_accession " +
                    "FROM scxa_cell_group_membership AS cell_group_membership " +
                        "INNER JOIN scxa_cell_group_marker_genes AS marker_genes " +
                            "ON marker_genes.cell_group_id = cell_group_membership.cell_group_id " +
                        "INNER JOIN scxa_cell_group_marker_gene_stats AS marker_gene_stats " +
                            "ON marker_genes.id = marker_gene_stats.marker_id " +
                    "WHERE marker_genes.marker_probability > 0.05 " +
                    "ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomSingleCellExperimentAccessionWithMarkerGenes() {
        return jdbcTemplate.queryForObject(
                "SELECT cell_group_membership.experiment_accession " +
                    "FROM scxa_cell_group_membership AS cell_group_membership " +
                        "INNER JOIN scxa_cell_group_marker_genes AS marker_genes " +
                            "ON marker_genes.cell_group_id = cell_group_membership.cell_group_id " +
                        "INNER JOIN scxa_cell_group_marker_gene_stats AS marker_gene_stats " +
                            "ON marker_genes.id = marker_gene_stats.marker_id " +
                    "WHERE marker_genes.marker_probability <= 0.05 " +
                    "ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomArrayDesignAccession() {
        return jdbcTemplate.queryForObject(
                "SELECT arraydesign FROM designelement_mapping ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public List<String> fetchPublicSpecies() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT species FROM experiment WHERE private = FALSE",
                String.class);
    }

    public String fetchRandomPublicSpecies() {
        return jdbcTemplate.queryForObject(
                "SELECT species FROM experiment WHERE private = FALSE ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    @Deprecated // Use fetchPublicspecies
    public List<String> fetchSpeciesForSingleCellExperiments() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT species FROM experiment",
                String.class);
    }

    @Deprecated // Use fetchRandomPublicSpecies
    public String fetchRandomSpeciesForSingleCellExperiments() {
        return jdbcTemplate.queryForObject(
                "SELECT species FROM experiment ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomGene() {
        return jdbcTemplate.queryForObject(
                "SELECT gene_id FROM scxa_analytics ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomGeneFromSingleCellExperiment(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT gene_id FROM scxa_analytics WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                String.class,
                experimentAccession);
    }

    public String fetchRandomMarkerGeneFromSingleCellExperiment(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT marker_genes.gene_id" +
                        "FROM scxa_cell_group_membership AS cell_group_membership" +
                        "         INNER JOIN scxa_cell_group_marker_genes AS marker_genes" +
                        "                    ON marker_genes.cell_group_id = cell_group_membership.cell_group_id" +
                        "         INNER JOIN scxa_cell_group_marker_gene_stats AS marker_gene_stats" +
                        "                    ON marker_genes.id = marker_gene_stats.marker_id" +
                        "WHERE cell_group_membership.experiment_accession =?" +
                        "AND marker_genes.marker_probability <= 0.05" +
                        "ORDER BY RANDOM() LIMIT  1;",
                String.class,
                experimentAccession);
    }

    public String fetchRandomCellFromExperiment(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT cell_id FROM scxa_analytics WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                String.class,
                experimentAccession);
    }

    public List<String> fetchRandomListOfCells(int numberOfCells) {
        return jdbcTemplate.queryForList(
                "SELECT cell_id FROM scxa_analytics ORDER BY RANDOM() LIMIT ?",
                String.class,
                numberOfCells);
    }

    public List<String> fetchRandomListOfCellsFromExperiment(String experimentAccession, int numberOfCells) {
        return jdbcTemplate.queryForList(
                "SELECT cell_id FROM scxa_analytics  WHERE experiment_accession=? ORDER BY RANDOM() LIMIT ?",
                String.class,
                experimentAccession,
                numberOfCells);
    }

    public int fetchRandomPerplexityFromExperimentTSne(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT perplexity FROM scxa_tsne WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                Integer.class,
                experimentAccession);
    }

    public int fetchRandomPerplexityFromExperimentTSne(String experimentAccession, String geneId) {
        return jdbcTemplate.queryForObject(
                "SELECT perplexity FROM scxa_tsne AS tsne " +
                        "LEFT JOIN scxa_analytics AS analytics " +
                        "ON analytics.experiment_accession=tsne.experiment_accession AND analytics.cell_id=tsne.cell_id " +
                        "WHERE tsne.experiment_accession=? AND analytics.gene_id=? ORDER BY RANDOM() LIMIT 1",
                Integer.class,
                experimentAccession, geneId);
    }

    public int fetchRandomKFromCellClusters(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT k FROM scxa_cell_clusters WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                Integer.class,
                experimentAccession);
    }

    public List<Integer> fetchKsFromCellClusters(String experimentAccession) {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT(k) FROM scxa_cell_clusters WHERE experiment_accession=?",
                Integer.class,
                experimentAccession);
    }

    public int fetchRandomKWithMarkerGene(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT h.variable as k_where_marker " +
                        "FROM scxa_cell_group_marker_genes m, scxa_cell_group h " +
                        "WHERE m.cell_group_id = h.id AND " +
                        "h.experiment_accession = ? AND m.marker_probability < 0.05 " +
                        "ORDER BY RANDOM() LIMIT 1",
                Integer.class,
                experimentAccession);
    }

    public String fetchRandomExperimentCollectionId() {
        return jdbcTemplate.queryForObject(
                "SELECT coll_id FROM collections ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomExperimentAccessionWithCollections() {
        return jdbcTemplate.queryForObject(
                "SELECT exp_acc FROM experiment2collection ORDER BY RANDOM() LIMIT 1",
                String.class);
    }
}