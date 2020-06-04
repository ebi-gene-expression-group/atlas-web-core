package uk.ac.ebi.atlas.solr.cloud.fullanalytics;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.web.RnaSeqBaselineRequestPreferences;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.ASSAY_GROUP_ID;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.fullanalytics.ExperimentRequestPreferencesSolrQueryFactory.createSolrQuery;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroups;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomEnsemblGeneId;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;

class ExperimentRequestPreferencesSolrQueryFactoryTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int ASSAY_GROUPS_MAX_COUNT = 1000;
    private static final int GENE_IDS_MAX_COUNT = 1000;

    @Test
    void throwsIfInstantiatedBecauseItsAUtilityClass() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(ExperimentRequestPreferencesSolrQueryFactory::new);
    }

    @Test
    void testDefaultQuery() {
        var experimentAccession = generateRandomExperimentAccession();
        RnaSeqBaselineRequestPreferences requestPreferences = new RnaSeqBaselineRequestPreferences();
        SolrQuery solrQuery = createSolrQuery(experimentAccession, requestPreferences, ImmutableSet.of());

        assertThat(solrQuery)
                .hasFieldOrPropertyWithValue(
                        "filterQueries",
                        new String[]{
                                "experiment_accession:(\"" + escapeQueryChars(experimentAccession) + "\")",
                                "expression_level:[" + requestPreferences.getDefaultCutoff() + " TO *]"})
                .hasFieldOrPropertyWithValue(
                        "fields",
                        "*")
                .hasFieldOrPropertyWithValue(
                        "rows",
                        SolrQueryBuilder.DEFAULT_ROWS);
    }

    @Test
    void testEmptyGeneQuery() {
        var experimentAccession = generateRandomExperimentAccession();
        var requestPreferences = new RnaSeqBaselineRequestPreferences();
        requestPreferences.setGeneQuery(SemanticQuery.create());
        var solrQuery = createSolrQuery(experimentAccession, requestPreferences, ImmutableSet.of());

        assertThat(solrQuery)
                .hasFieldOrPropertyWithValue(
                        "filterQueries",
                        new String[]{
                                "experiment_accession:(\"" + escapeQueryChars(experimentAccession) + "\")",
                                "expression_level:[" + requestPreferences.getDefaultCutoff() + " TO *]"})
                .hasFieldOrPropertyWithValue(
                        "query",
                        "*:*")
                .hasFieldOrPropertyWithValue(
                        "fields",
                        "*")
                .hasFieldOrPropertyWithValue(
                        "rows",
                        SolrQueryBuilder.DEFAULT_ROWS);
    }

    @Test
    void testQueriesAreJoinedWithAnd() {
        var experimentAccession = generateRandomExperimentAccession();
        var assayGroupsIds =
                generateRandomAssayGroups(RNG.nextInt(1, ASSAY_GROUPS_MAX_COUNT)).stream()
                        .map(AssayGroup::getId)
                        .collect(toImmutableSet());
        var geneIds = IntStream.range(0, RNG.nextInt(1, GENE_IDS_MAX_COUNT)).boxed()
                .map(__ -> generateRandomEnsemblGeneId())
                .collect(toImmutableSet());

        var requestPreferences = new RnaSeqBaselineRequestPreferences();
        requestPreferences.setSelectedColumnIds(assayGroupsIds);

        var solrQuery = createSolrQuery(experimentAccession, requestPreferences, geneIds);
        var explodedQuery = Arrays.asList(solrQuery.getQuery().split(" AND "));

        assertThat(explodedQuery)
                .hasSize(2)
                .allMatch(query -> query.matches("\\(\\w+:.+\\)"));

        assertThat(
                explodedQuery.stream()
                        .filter(query -> query.startsWith("(" + ASSAY_GROUP_ID.name()))
                        .findFirst()
                        .orElse("")
                        .split(":")[1].split(" OR "))
                .hasSameSizeAs(assayGroupsIds);
        assertThat(
                explodedQuery.stream()
                        .filter(query -> query.startsWith("(" + BIOENTITY_IDENTIFIER.name()))
                        .findFirst()
                        .orElse("")
                        .split(":")[1].split(" OR "))
                .hasSameSizeAs(geneIds);
    }

    // TODO test gene query OR’s the fields
}
