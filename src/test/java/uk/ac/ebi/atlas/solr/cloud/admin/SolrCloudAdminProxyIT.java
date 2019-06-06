package uk.ac.ebi.atlas.solr.cloud.admin;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SolrCloudAdminProxyIT {
    @Inject
    private SolrCloudAdminProxy subject;

    @Test
    void validCollectionNamesWithoutAliases() throws IOException, SolrServerException {
        assertThat(subject.areCollectionsUp(
                ImmutableSet.of("bioentities"),
                ImmutableSet.of()))
                .isTrue();
    }

    @Test
    void validCollectionNamesWithAliases() throws IOException, SolrServerException {
        assertThat(
                subject.areCollectionsUp(
                        ImmutableSet.of("bioentities"),
                        ImmutableSet.of("bulk-analytics", "scxa-analytics", "scxa-gene2experiment")))
                .isTrue();
    }

    @Test
    void invalidCollectionName() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subject.areCollectionsUp(ImmutableSet.of("foo"), ImmutableSet.of()));
    }

    @Test
    void invalidAlias() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subject.areCollectionsUp(ImmutableSet.of(), ImmutableSet.of("foo")));
    }
}
