package uk.ac.ebi.atlas.search.bioentities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.testutils.JdbcUtils;
import uk.ac.ebi.atlas.testutils.SolrUtils;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.solr.BioentityPropertyName.GENE_BIOTYPE;
import static uk.ac.ebi.atlas.solr.BioentityPropertyName.GOTERM;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BioentitiesSearchDaoIT {
    @Inject
    private DataSource dataSource;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    private SolrUtils solrUtils;

    @Inject
    private SolrCloudCollectionProxyFactory collectionProxyFactory;

    @Inject
    private SpeciesFactory speciesFactory;

    @Inject
    private BioentitiesSearchDao subject;

    private BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;

    @BeforeAll
    void populateDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/gxa-experiment-fixture.sql"));
        populator.execute(dataSource);

        bulkAnalyticsCollectionProxy = collectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
    }

    @AfterAll
    void cleanDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/experiment-delete.sql"));
        populator.execute(dataSource);
    }

    @Test
    void searchGeneIdsReturnsExperimentGenesOnly() {
        var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
        var geneId = solrUtils.fetchRandomGeneIdFromAnalytics(experimentAccession);

        var solrQueryBuilder =
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                    .addQueryFieldByTerm(BIOENTITY_IDENTIFIER, geneId)
                    .setFieldList(EXPERIMENT_ACCESSION);
        var results = bulkAnalyticsCollectionProxy.query(solrQueryBuilder).getResults();

        assertThat(results)
                .isNotEmpty()
                .anyMatch(solrDocument -> solrDocument.containsValue(experimentAccession));
    }

    @Test
    void geneIdsAreNarrowedBySearchQuery() {
        var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();
        var allGeneIds = solrUtils.fetchAllGeneIdsFromAnalytics(experimentAccession);

        var results = subject.searchGeneIds(
                experimentAccession,
                SemanticQuery.create(SemanticQueryTerm.create("protein_coding", GENE_BIOTYPE.name())));

        assertThat(results)
                .size().isLessThan(allGeneIds.size());
    }

    @Test
    void searchSpeciesWithBroadTerm() {
        assertThat(subject.searchSpecies(SemanticQuery.create(SemanticQueryTerm.create("expression"))))
                .contains(speciesFactory.create("Homo sapiens").getEnsemblName())
                .contains(speciesFactory.create("Mus musculus").getEnsemblName());
    }

    @Test
    void searchSpeciesWithNarrowTerm() {
        assertThat(subject.searchSpecies(SemanticQuery.create(SemanticQueryTerm.create("ASPM", "symbol"))))
                .contains(speciesFactory.create("Homo sapiens").getEnsemblName())
                .contains(speciesFactory.create("Mus musculus").getEnsemblName())
                .doesNotContain(speciesFactory.create("Arabidopsis thaliana").getEnsemblName());

        assertThat(subject.searchSpecies(SemanticQuery.create(SemanticQueryTerm.create("FBgn0038395", "flybase_gene_id"))))
                .containsExactlyInAnyOrder(speciesFactory.create("Drosophila melanogaster").getEnsemblName());
    }

    @Test
    void emptyQueryReturnsNoDocuments() {
        assertThat(subject.searchGeneIds(generateRandomExperimentAccession(), SemanticQuery.create()))
                .isEmpty();
        assertThat(subject.searchSpecies(SemanticQuery.create()))
                .isEmpty();
    }
}
