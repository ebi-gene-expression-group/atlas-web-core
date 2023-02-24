package uk.ac.ebi.atlas.solr.bioentities.query;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;

import javax.inject.Inject;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class BioentitiesSolrClientIT {
    @Inject
    private BioentitiesSolrClient subject;

    @Test
    public void testGetBioentityIdentifiers() {
        Set<String> result = subject.getBioentityIdentifiers(BioentityPropertyName.MGI_ID, "MGI:3615484");

        assertThat(result.size(), Matchers.equalTo(1));
        assertThat(result, Matchers.contains("ENSMUSG00000033450"));
    }
}
