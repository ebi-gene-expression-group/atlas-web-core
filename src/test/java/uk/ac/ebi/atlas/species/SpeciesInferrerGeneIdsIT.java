package uk.ac.ebi.atlas.species;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.search.SemanticQuery;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = TestConfig.class)
public class SpeciesInferrerGeneIdsIT {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Inject
    private SpeciesInferrer subject;

    private final String geneId;
    private final String expectedSpecies;

    public SpeciesInferrerGeneIdsIT(String geneId, String expectedSpecies) {
        this.geneId = geneId;
        this.expectedSpecies = expectedSpecies;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "ENSMUSG00000019082", "mus musculus" },
                { "FBgn0260743", "drosophila melanogaster" },
                { "ENSTNIG00000000963", "tetraodon nigroviridis" }
        });
    }

    @Test
    public void inferSpeciesForGeneId() {
        assertThat(
                subject.inferSpeciesForGeneQuery(SemanticQuery.create(geneId)).getReferenceName(),
                is(expectedSpecies));
    }
}
