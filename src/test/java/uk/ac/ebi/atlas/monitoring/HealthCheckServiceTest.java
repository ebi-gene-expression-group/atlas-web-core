package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.experimentimport.ExperimentDao;
import uk.ac.ebi.atlas.solr.cloud.admin.SolrCloudAdminProxy;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {
    @Mock
    private SolrCloudAdminProxy solrCloudAdminProxyMock;

    @Mock
    private ExperimentDao experimentDaoMock;

    private HealthCheckService subject;

    private static final ImmutableSet<String> MOCK_SOLR_COLLECTIONS =
            ImmutableSet.of("mockCollection1", "mockCollection2");
    private static final ImmutableSet<String> MOCK_SOLR_COLLECTION_ALIAS = ImmutableSet.of("mockCollectionAlias");

    @BeforeEach
    void setUp() {
        subject = new HealthCheckService(solrCloudAdminProxyMock);
    }

    @Test
    void solrCollectionsAreUp() throws IOException, SolrServerException {
        when(solrCloudAdminProxyMock.areCollectionsUp(anyCollection(), anyCollection())).thenReturn(true);
        assertThat(subject.isSolrUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS)).isTrue();
    }

    @Test
    void solrCollectionsAreDown() throws IOException, SolrServerException {
        when(solrCloudAdminProxyMock.areCollectionsUp(anyCollection(), anyCollection())).thenReturn(false);
        assertThat(subject.isSolrUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS)).isFalse();
    }

    @Test
    void solrThrowsException() throws IOException, SolrServerException {
        when(solrCloudAdminProxyMock.areCollectionsUp(anyCollection(), anyCollection()))
                .thenThrow(RuntimeException.class);
        assertThat(subject.isSolrUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS)).isFalse();
    }

    @Test
    void experimentsDatabaseIsUp() {
        when(experimentDaoMock.countExperiments()).thenReturn(9);
        assertThat(subject.isDatabaseUp(experimentDaoMock)).isTrue();
    }

    @Test
    void noExperimentsInDatabase() {
        when(experimentDaoMock.countExperiments()).thenReturn(0);
        assertThat(subject.isDatabaseUp(experimentDaoMock)).isFalse();
    }

    @Test
    void experimentDaoThrowsException() {
        when(experimentDaoMock.countExperiments()).thenThrow(RuntimeException.class);
        assertThat(subject.isDatabaseUp(experimentDaoMock)).isFalse();
    }
}
