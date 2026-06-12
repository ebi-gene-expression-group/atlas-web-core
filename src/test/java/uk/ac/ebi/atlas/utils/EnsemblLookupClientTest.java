package uk.ac.ebi.atlas.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.species.SpeciesProperties;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnsemblLookupClientTest {
    private static final String ENSEMBL_BASE_URL = "https://rest.ensembl.org";
    private static final String ENSTNIG = "ENSTNIG00000000963";

    @Mock
    private RestTemplate restTemplateMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    private EnsemblLookupClient subject;

    @Before
    public void setUp() {
        subject = new EnsemblLookupClient(restTemplateMock, speciesFactoryMock, ENSEMBL_BASE_URL);
    }

    @Test
    public void isEnsemblIdReturnsTrueForEnsPrefix() {
        assertTrue(EnsemblLookupClient.isEnsemblId(ENSTNIG));
    }

    @Test
    public void isEnsemblIdReturnsFalseForNonEnsIds() {
        assertFalse(EnsemblLookupClient.isEnsemblId("FBgn0260743"));
        assertFalse(EnsemblLookupClient.isEnsemblId(null));
    }

    @Test
    public void returnsEmptyForNonEnsemblId() {
        assertFalse(subject.lookupSpecies("FBgn0260743").isPresent());
    }

    @Test
    public void returnsSpeciesFromEnsemblResponse() {
        var species = new Species("tetraodon_nigroviridis", SpeciesProperties.UNKNOWN);

        when(restTemplateMock.exchange(
                eq(ENSEMBL_BASE_URL + "/lookup/id/" + ENSTNIG + "?content-type=application/json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"species\":\"tetraodon_nigroviridis\"}"));
        when(speciesFactoryMock.create("tetraodon_nigroviridis")).thenReturn(species);

        Optional<Species> result = subject.lookupSpecies(ENSTNIG);

        assertTrue(result.isPresent());
        assertThat(result.get(), is(species));
    }

    @Test
    public void returnsEmptyIfEnsemblRequestFails() {
        when(restTemplateMock.exchange(
                eq(ENSEMBL_BASE_URL + "/lookup/id/" + ENSTNIG + "?content-type=application/json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Timeout"));

        assertFalse(subject.lookupSpecies(ENSTNIG).isPresent());
    }

    @Test
    public void returnsEmptyIfEnsemblResponseHasNoSpeciesField() {
        when(restTemplateMock.exchange(
                eq(ENSEMBL_BASE_URL + "/lookup/id/" + ENSTNIG + "?content-type=application/json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"id\":\"" + ENSTNIG + "\"}"));

        assertFalse(subject.lookupSpecies(ENSTNIG).isPresent());
    }

    @Test
    public void returnsEmptyIfEnsemblResponseIsInvalidJson() {
        when(restTemplateMock.exchange(
                eq(ENSEMBL_BASE_URL + "/lookup/id/" + ENSTNIG + "?content-type=application/json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("not-json"));

        assertFalse(subject.lookupSpecies(ENSTNIG).isPresent());
    }
}
