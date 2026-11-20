package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EnsemblSpeciesTraderTest {
    private EnsemblSpeciesTrader subject;

    @Before
    public void setUp() {
        subject = new EnsemblSpeciesTrader(ImmutableMap.of(
                "ENS", "Homo sapiens",
                "ENSMUS", "Mus musculus",
                "ENSCSA", "Chlorocebus sabaeus",
                "ENSCSAV", "Ciona savignyi",
                "ENSTNI", "Tetraodon nigroviridis"));
    }

    @Test
    public void resolvesHumanEnsPrefix() {
        assertThat(subject.speciesNameForEnsemblId("ENSG00000198189").get(), is("Homo sapiens"));
    }

    @Test
    public void prefersLongestMatchingPrefix() {
        assertThat(
                subject.speciesNameForEnsemblId("ENSCSAVG00000005771").get(),
                is("Ciona savignyi"));
        assertThat(
                subject.speciesNameForEnsemblId("ENSCSAG00000014252").get(),
                is("Chlorocebus sabaeus"));
    }

    @Test
    public void resolvesMouseAndTetraodon() {
        assertThat(subject.speciesNameForEnsemblId("ENSMUSG00000029311").get(), is("Mus musculus"));
        assertThat(subject.speciesNameForEnsemblId("ENSTNIG00000000963").get(), is("Tetraodon nigroviridis"));
    }

    @Test
    public void acceptsOptionalVersionSuffix() {
        assertThat(subject.speciesNameForEnsemblId("ENSG00000198189.12").get(), is("Homo sapiens"));
    }

    @Test
    public void geneTreeAndProteinFamilyIdsAreMisses() {
        assertThat(subject.speciesNameForEnsemblId("ENSGT00940000155444").isPresent(), is(false));
        assertThat(subject.speciesNameForEnsemblId("ENSFM00250000000001").isPresent(), is(false));
    }

    @Test
    public void unknownPrefixIsMiss() {
        assertThat(subject.speciesNameForEnsemblId("ENSDARG00000008306").isPresent(), is(false));
    }

    @Test
    public void nullAndEmptyAreMisses() {
        assertThat(subject.speciesNameForEnsemblId(null).isPresent(), is(false));
        assertThat(subject.speciesNameForEnsemblId("").isPresent(), is(false));
    }
}
