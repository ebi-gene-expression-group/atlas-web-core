package uk.ac.ebi.atlas.experimentimport.idf;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.testutils.MockDataFileHub;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class IdfParserTest {

    private static final String E_MTAB_513 = "E-MTAB-513";

    private static final String INVESTIGATION_TITLE =
            "RNA-Seq of human individual tissues and mixture of 16 tissues (Illumina Body Map)";
    private static final String AE_DISPLAY_NAME =
            "Study investigating RNA-Seq of human individual tissues and mixture of 16 tissues";
    private static final String[] PUBMED_IDS_ARRAY =
            {"22496456", "22955988", "23258890"};
    private static final String[] PUBLICATIONS_ARRAY =
            {"Publication 1", "Another publication", "Yet another publication"};
    private static final ImmutableSet<String> PUBMED_IDS =
            ImmutableSet.copyOf(PUBMED_IDS_ARRAY);
    private static final String EXPECTED_CLUSTERS =
            "5";
    private static final String[] ADDITIONAL_ATTRIBUTES =
            {"individual", "genotype", "FACS marker"};

    private static final String[][] IDF_TXT = {
            {"Investigation Title", INVESTIGATION_TITLE},
            {"Comment[AEExperimentDisplayName]", AE_DISPLAY_NAME},
            {"PubMed ID", PUBMED_IDS_ARRAY[0], PUBMED_IDS_ARRAY[1], PUBMED_IDS_ARRAY[2]},
            {"Publication Title", PUBLICATIONS_ARRAY[0], PUBLICATIONS_ARRAY[1], PUBLICATIONS_ARRAY[2]},
            {"Comment[EAExpectedClusters]", EXPECTED_CLUSTERS},
            {
                "Comment[EAAdditionalAttributes]",
                ADDITIONAL_ATTRIBUTES[0], ADDITIONAL_ATTRIBUTES[1], ADDITIONAL_ATTRIBUTES[2]
            }
    };

    private static final String[][] IDF_TXT_WHITESPACES_IN_COMMENTS = {
            {"Investigation Title", INVESTIGATION_TITLE},
            {"Comment [AEExperimentDisplayName]", AE_DISPLAY_NAME},
            {"Comment [EAExpectedClusters]", EXPECTED_CLUSTERS},
    };

    private static final String[][] IDF_TXT_SPACES_IN_FIELD_VALUES = {
            {"Investigation Title", INVESTIGATION_TITLE},
            {"Comment [AEExperimentDisplayName]", "     "},
    };

    private static final String[][] IDF_TXT_MIXED_CASE = {
            {"INVESTIGATION INVESTIGATION_TITLE   ", INVESTIGATION_TITLE},
            {" comment[AEExperimentDisplayName]", AE_DISPLAY_NAME},
            {"PubMed id", PUBMED_IDS_ARRAY[0], PUBMED_IDS_ARRAY[1], PUBMED_IDS_ARRAY[2]},
            {"publication title", PUBLICATIONS_ARRAY[0], PUBLICATIONS_ARRAY[1], PUBLICATIONS_ARRAY[2]},
            {"comment[eaexpectedclusters]", EXPECTED_CLUSTERS}
    };

    private static final String[][] IDF_TXT_DUPLICATE_FIELDS = {
            {"Investigation Title", INVESTIGATION_TITLE},
            {"Comment[AEExperimentDisplayName]", AE_DISPLAY_NAME},
            {"Comment[AEExperimentDisplayName]", "Foobar"},
            {"PubMed ID", PUBMED_IDS_ARRAY[0], PUBMED_IDS_ARRAY[1], PUBMED_IDS_ARRAY[2]},
            {"Publication Title", PUBLICATIONS_ARRAY[0], PUBLICATIONS_ARRAY[1], PUBLICATIONS_ARRAY[2]},
            {"Comment[EAExpectedClusters]", EXPECTED_CLUSTERS},
            {
                "Comment[EAAdditionalAttributes]",
                ADDITIONAL_ATTRIBUTES[0], ADDITIONAL_ATTRIBUTES[1], ADDITIONAL_ATTRIBUTES[2]
            }
    };

    private static final String[][] IDF_TXT_EMPTY_DISPLAY_NAME = {
            {"Investigation Title", INVESTIGATION_TITLE},
            {"Comment[AEExperimentDisplayName]", ""}
    };

    private MockDataFileHub dataFileHub;

    private IdfParser subject;

    @BeforeEach
    void setUp() {
        dataFileHub = MockDataFileHub.create();

        subject = new IdfParser(dataFileHub);
    }

    @Test
    void parse() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(AE_DISPLAY_NAME);
        assertThat(idfParserOutput.getPubmedIds()).containsOnlyElementsOf(PUBMED_IDS);
        assertThat(idfParserOutput.getExpectedClusters()).isEqualTo(NumberUtils.toInt(EXPECTED_CLUSTERS));
        assertThat(idfParserOutput.getMetadataFieldsOfInterest())
                .containsOnlyElementsOf(ImmutableSet.copyOf(ADDITIONAL_ATTRIBUTES));
    }

    @Test
    void parseMixedCaseKeys() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT_MIXED_CASE));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(AE_DISPLAY_NAME);
        assertThat(idfParserOutput.getPubmedIds()).containsOnlyElementsOf(PUBMED_IDS);
        assertThat(idfParserOutput.getExpectedClusters()).isEqualTo(NumberUtils.toInt(EXPECTED_CLUSTERS));
    }

    @Test
    void parseNoPubmedIds() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT[0], IDF_TXT[1]));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(AE_DISPLAY_NAME);
        assertThat(idfParserOutput.getPublications()).isEmpty();
    }

    @Test
    void parseNoAeDisplayName() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT[0], IDF_TXT[2], IDF_TXT[3]));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(INVESTIGATION_TITLE);
        assertThat(idfParserOutput.getPubmedIds()).containsOnlyElementsOf(PUBMED_IDS);
    }

    @Test
    void parseNoAeDisplayNameNoTitle() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT[2], IDF_TXT[3]));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEmpty();
        assertThat(idfParserOutput.getPubmedIds()).containsOnlyElementsOf(PUBMED_IDS);
    }

    @Test
    void parseNothing() {
        dataFileHub.addIdfFile(E_MTAB_513, Collections.emptyList());

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEmpty();
        assertThat(idfParserOutput.getPublications()).isEmpty();
        assertThat(idfParserOutput.getExpectedClusters()).isEqualTo(0);
    }

    @Test
    void keepsOnlyFirstValueOfDuplicateFields() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT_DUPLICATE_FIELDS));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(AE_DISPLAY_NAME);
    }

    @Test
    void skipsEmptyDisplayName() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT_EMPTY_DISPLAY_NAME));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(INVESTIGATION_TITLE);
    }

    @Test
    void parsesCommentsWithWhitespaces() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT_WHITESPACES_IN_COMMENTS));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(AE_DISPLAY_NAME);
        assertThat(idfParserOutput.getExpectedClusters()).isEqualTo(NumberUtils.toInt(EXPECTED_CLUSTERS));
    }

    @Test
    void ignoresFieldsWithEmptyValuesMadeUpOfWhitespaces() {
        dataFileHub.addIdfFile(E_MTAB_513, Arrays.asList(IDF_TXT_SPACES_IN_FIELD_VALUES));

        IdfParserOutput idfParserOutput = subject.parse(E_MTAB_513);

        assertThat(idfParserOutput.getTitle()).isEqualTo(INVESTIGATION_TITLE);
    }
}
