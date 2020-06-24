package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableSet;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomPrideExperimentAccession;

@ExtendWith(MockitoExtension.class)
class LinkToPrideIT {
    private LinkToPride subject;

    @Mock
    BaselineExperiment baselineExperimentMock;

    @BeforeEach
    void setUp() {
        subject = new LinkToPride();
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void iconAndLinkPointAtPride() {
        var accession = ImmutableSet.of(generateRandomPrideExperimentAccession());
        when(baselineExperimentMock.getSecondaryAccessions()).thenReturn(accession);
        assertThat(subject.get(baselineExperimentMock))
                .hasSize(1)
                .first()
                .hasFieldOrProperty("uri")
                .hasFieldOrProperty("description");

        assertThat(subject.get(baselineExperimentMock).iterator().next().uri)
                .hasToString("redirect:https://www.ebi.ac.uk/pride/archive/projects/" + accession);

        assertThat(subject.get(baselineExperimentMock).iterator().next().description)
                .hasFieldOrPropertyWithValue("type", "icon-pride")
                .hasFieldOrPropertyWithValue("description", "PRIDE Archive: project " + accession);
    }

    @Test
    void goesIntoSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}