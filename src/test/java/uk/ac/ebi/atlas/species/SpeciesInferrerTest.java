package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.bioentities.query.SolrQueryService;
import uk.ac.ebi.atlas.utils.EnsemblLookupClient;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpeciesInferrerTest {
    private static final String ENSTNIG = "ENSTNIG00000000963";
    private static final String TETRAODON = "tetraodon nigroviridis";
    private static final String MOUSE = "mus musculus";

    @Mock
    private SolrQueryService bioentitiesSearchServiceMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    @Mock
    private SpeciesFinder speciesFinderMock;

    @Mock
    private EnsemblLookupClient ensemblLookupClientMock;

    private SpeciesInferrer subject;

    @Before
    public void setUp() {
        subject = new SpeciesInferrer(
                bioentitiesSearchServiceMock,
                speciesFactoryMock,
                speciesFinderMock,
                ensemblLookupClientMock);
    }

    private static Species speciesWithReferenceName(String referenceName) {
        return new Species(
                referenceName,
                SpeciesProperties.create(referenceName.replace(' ', '_'), "", "", ImmutableList.of()));
    }

    @Test
    public void usesEnsemblForEnsIdsBeforeSolr() {
        var tetraodonSpecies = speciesWithReferenceName(TETRAODON);
        var queryTerm = SemanticQueryTerm.create(ENSTNIG);

        when(ensemblLookupClientMock.lookupSpecies(ENSTNIG)).thenReturn(Optional.of(tetraodonSpecies));
        when(speciesFactoryMock.create(TETRAODON)).thenReturn(tetraodonSpecies);

        Species result = subject.inferSpeciesForGeneQuery(SemanticQuery.create(queryTerm));

        assertThat(result.getReferenceName(), is(TETRAODON));
        verify(bioentitiesSearchServiceMock, never()).fetchSpecies(any());
        verify(speciesFinderMock, never()).findSpecies(any(), any());
    }

    @Test
    public void fallsBackToSolrWhenEnsemblLookupMisses() {
        var mouseSpecies = speciesWithReferenceName(MOUSE);
        var queryTerm = SemanticQueryTerm.create("ENSMUSG00000002055");

        when(speciesFinderMock.findSpecies(any(), any())).thenReturn(ImmutableList.of());
        when(ensemblLookupClientMock.lookupSpecies("ENSMUSG00000002055")).thenReturn(Optional.empty());
        when(bioentitiesSearchServiceMock.fetchSpecies(queryTerm)).thenReturn(Set.of(MOUSE));
        when(speciesFactoryMock.create(MOUSE)).thenReturn(mouseSpecies);

        Species result = subject.inferSpeciesForGeneQuery(SemanticQuery.create(queryTerm));

        assertThat(result.getReferenceName(), is(MOUSE));
    }

    @Test
    public void usesSolrDirectlyForNonEnsemblIds() {
        var flySpecies = speciesWithReferenceName("drosophila melanogaster");
        var queryTerm = SemanticQueryTerm.create("FBgn0260743");

        when(speciesFinderMock.findSpecies(any(), any())).thenReturn(ImmutableList.of());
        when(bioentitiesSearchServiceMock.fetchSpecies(queryTerm)).thenReturn(Set.of("drosophila melanogaster"));
        when(speciesFactoryMock.create("drosophila melanogaster")).thenReturn(flySpecies);

        Species result = subject.inferSpeciesForGeneQuery(SemanticQuery.create(queryTerm));

        assertThat(result.getReferenceName(), is("drosophila melanogaster"));
        verify(ensemblLookupClientMock, never()).lookupSpecies(any());
    }

    @Test
    public void returnsUnknownWhenMultipleSpeciesAreFound() {
        when(speciesFinderMock.findSpecies(any(), any())).thenReturn(ImmutableList.of());
        when(ensemblLookupClientMock.lookupSpecies("ENSMUSG00000002055"))
                .thenReturn(Optional.of(speciesWithReferenceName(MOUSE)));
        when(ensemblLookupClientMock.lookupSpecies(ENSTNIG))
                .thenReturn(Optional.of(speciesWithReferenceName(TETRAODON)));
        when(speciesFactoryMock.createUnknownSpecies()).thenReturn(new Species("", SpeciesProperties.UNKNOWN));

        Species result = subject.inferSpeciesForGeneQuery(SemanticQuery.create(
                SemanticQueryTerm.create("ENSMUSG00000002055"),
                SemanticQueryTerm.create(ENSTNIG)));

        assertThat(result.isUnknown(), is(true));
    }

    @Test
    public void ensemblCandidatesTakePrecedenceOverAnalytics() {
        var tetraodonSpecies = speciesWithReferenceName(TETRAODON);

        when(ensemblLookupClientMock.lookupSpecies(ENSTNIG)).thenReturn(Optional.of(tetraodonSpecies));
        when(speciesFinderMock.findSpecies(any(), any())).thenReturn(ImmutableList.of("homo sapiens"));
        when(speciesFactoryMock.create(TETRAODON)).thenReturn(tetraodonSpecies);

        Species result = subject.inferSpeciesForGeneQuery(SemanticQuery.create(ENSTNIG));

        assertThat(result.getReferenceName(), is(TETRAODON));
        verify(speciesFinderMock, never()).findSpecies(any(), any());
        verify(bioentitiesSearchServiceMock, never()).fetchSpecies(any());
    }
}
