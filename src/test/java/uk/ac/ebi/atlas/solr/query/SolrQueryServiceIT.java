package uk.ac.ebi.atlas.solr.query;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.bioentities.query.SolrQueryService;

import javax.inject.Inject;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class SolrQueryServiceIT {
    @Inject
    private SolrQueryService subject;

    @Test
    public void getKnownSpeciesWithCategory() {
        assertThat(
                subject.fetchSpecies(
                        SemanticQueryTerm.create("ENSMUSG00000019082", BioentityPropertyName.ENSGENE.name())),
                hasItem(equalToIgnoringCase("mus_musculus")));

        assertThat(
                subject.fetchSpecies(
                        SemanticQueryTerm.create("FBgn0260743", BioentityPropertyName.FLYBASE_GENE_ID.name())),
                hasItem(equalToIgnoringCase("drosophila_melanogaster")));
    }

    @Test
    public void getKnownSpeciesWithoutCategory() {
        assertThat(
                subject.fetchSpecies(SemanticQueryTerm.create("ENSMUSG00000019082")),
                hasItem(equalToIgnoringCase("mus_musculus")));
    }

    // Brittle test: order of Solr collection will change results
    @Disabled
    @Test
    public void queryWithoutCategoryFallsBackToProperties() {
        assertThat(
                subject.fetchSpecies(SemanticQueryTerm.create("OBP3-responsive gene 4")),
                hasItem(equalToIgnoringCase("arabidopsis_lyrata")));
    }

    @Test
    public void unknownSpeciesReturnsEmpty() {
        // Escherichia coli
        assertThat(
                subject.fetchSpecies(SemanticQueryTerm.create(BioentityPropertyName.ENSGENE.name(), "ECBD_0176")),
                is(empty()));
    }
}
