package uk.ac.ebi.atlas.species;

import au.com.bytecode.opencsv.CSVReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class EnsemblSpeciesTsvParserTest {
    private EnsemblSpeciesTsvParser subject;

    @Mock
    private CSVReader tsvReaderMock;

    @Before
    public void setUp() {
        subject = new EnsemblSpeciesTsvParser(tsvReaderMock);
    }

    @Test
    public void parsesPrefixAndSpeciesName() throws Exception {
        given(tsvReaderMock.readNext())
                .willReturn(new String[] {"ENSMUS", "Mus musculus"})
                .willReturn(new String[] {"ENS", "Homo sapiens"})
                .willReturn(null);

        Map<String, String> map = subject.parse();

        assertThat(map.size(), is(2));
        assertThat(map.get("ENSMUS"), is("Mus musculus"));
        assertThat(map.get("ENS"), is("Homo sapiens"));
    }

    @Test
    public void acceptsMgpPrefix() throws Exception {
        given(tsvReaderMock.readNext())
                .willReturn(new String[] {"MGP_PahariEiJ_", "Mus pahari"})
                .willReturn(null);

        Map<String, String> map = subject.parse();

        assertThat(map.get("MGP_PahariEiJ_"), is("Mus pahari"));
    }

    @Test
    public void ignoresLinesWithoutValidPrefix() throws Exception {
        given(tsvReaderMock.readNext())
                .willReturn(new String[] {"FOO", "Bar"})
                .willReturn(new String[] {"ENS", "Homo sapiens"})
                .willReturn(null);

        Map<String, String> map = subject.parse();

        assertThat(map.size(), is(1));
        assertThat(map.get("ENS"), is("Homo sapiens"));
    }

    @Test
    public void ignoresBlankLinesAndMissingSpecies() throws Exception {
        given(tsvReaderMock.readNext())
                .willReturn(new String[] {})
                .willReturn(new String[] {""})
                .willReturn(new String[] {"ENS", ""})
                .willReturn(new String[] {"ENSMUS", "Mus musculus"})
                .willReturn(null);

        Map<String, String> map = subject.parse();

        assertThat(map.size(), is(1));
        assertThat(map.get("ENSMUS"), is("Mus musculus"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicatePrefixIsRejected() throws Exception {
        given(tsvReaderMock.readNext())
                .willReturn(new String[] {"ENSMUS", "Mus musculus"})
                .willReturn(new String[] {"ENSMUS", "Mus musculus domesticus"})
                .willReturn(null);

        subject.parse();
    }
}
