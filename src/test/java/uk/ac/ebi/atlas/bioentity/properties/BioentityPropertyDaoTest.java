package uk.ac.ebi.atlas.bioentity.properties;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.atlas.controllers.BioentityNotFoundException;
import uk.ac.ebi.atlas.solr.bioentities.query.BioentitiesSolrClient;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.bioentity.properties.BioEntityCardProperties.BIOENTITY_PROPERTY_NAMES;
import static uk.ac.ebi.atlas.solr.BioentityPropertyName.ENSGENE;
import static uk.ac.ebi.atlas.solr.BioentityPropertyName.SYMBOL;

@RunWith(MockitoJUnitRunner.class)
public class BioentityPropertyDaoTest {
    private static <K, V> HashMap<K, V> hashMapOf() {
        return new HashMap<>();
    }

    private static <K, V> HashMap<K, V> hashMapOf(K key, V value) {
        HashMap<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static final String ID_IN_BIOENTITIES = "ENSG00000132604";
    private static final String ID_IN_BIOENTITIES_SYMBOL = "TERF2";
    private static final String ID_IN_ANALYTICS = "ENSG00000005955";

    @Mock
    private BioentitiesSolrClient bioentitiesCollectionMock;

    @Mock
    private SolrCloudCollectionProxyFactory collectionProxyFactoryMock;

    @Mock
    private ExpressedBioentityFinder expressedBioentityFinderMock;

    @Mock
    private QueryResponse oneResultQueryResponseMock;

    @Mock
    private QueryResponse noResultsQueryResponseMock;

    private BioEntityPropertyDao subject;

    @Before
    public void setUp() throws Exception {
        when(bioentitiesCollectionMock.getMap(ID_IN_BIOENTITIES, BIOENTITY_PROPERTY_NAMES))
                .thenReturn(hashMapOf(SYMBOL, ImmutableSet.of(ID_IN_BIOENTITIES_SYMBOL)));
        when(bioentitiesCollectionMock.getMap(not(eq(ID_IN_BIOENTITIES)), anyList()))
                .thenReturn(hashMapOf());

        SolrDocumentList oneResultSolrDocumentList = new SolrDocumentList();
        oneResultSolrDocumentList.add(new SolrDocument(hashMapOf("bioentity_identifier", ID_IN_BIOENTITIES)));
        when(oneResultQueryResponseMock.getResults()).thenReturn(oneResultSolrDocumentList);
        when(noResultsQueryResponseMock.getResults()).thenReturn(new SolrDocumentList());

        subject =
                new BioEntityPropertyDao(
                        bioentitiesCollectionMock, collectionProxyFactoryMock, expressedBioentityFinderMock);
    }

    @Test(expected = BioentityNotFoundException.class)
    public void geneIdNotFoundInBioentitiesNorAnalyticsCollectionThrows() {
        when(expressedBioentityFinderMock.bioentityIsExpressedInAtLeastOneExperiment("ENSFOOBAR"))
                .thenReturn(false);

        subject.fetchGenePageProperties("ENSFOOBAR");
    }

    @Test
    public void ifGeneIdInBioentitiesWeDontQueryAnalytics() {
        assertThat(subject.fetchGenePageProperties(ID_IN_BIOENTITIES))
                .containsOnlyKeys(SYMBOL)
                .containsValues(ImmutableSet.of(ID_IN_BIOENTITIES_SYMBOL));

        verifyZeroInteractions(expressedBioentityFinderMock);
    }

    @Test
    public void ifGeneIdNotInBioentitiesButInAnalytics() {
        when(expressedBioentityFinderMock.bioentityIsExpressedInAtLeastOneExperiment(ID_IN_ANALYTICS))
                .thenReturn(true);

        assertThat(subject.fetchGenePageProperties(ID_IN_ANALYTICS))
                .containsOnlyKeys(ENSGENE);
    }
}
