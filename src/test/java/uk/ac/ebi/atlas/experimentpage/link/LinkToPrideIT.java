package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;

import java.net.URI;
import java.net.URISyntaxException;

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

    @Test
    void oneLinkAndIconPointAtPride() throws URISyntaxException {
        var accession = generateRandomPrideExperimentAccession();
        when(baselineExperimentMock.getSecondaryAccessions()).thenReturn(ImmutableSet.of(accession));
        assertThat(subject.get(baselineExperimentMock))
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue(
                        "uri",
                        new URI("redirect:https://www.ebi.ac.uk/pride/archive/projects/" + accession))
                .hasFieldOrPropertyWithValue(
                        "description",
                        ExternallyAvailableContent.Description.create("icon-pride", "PRIDE Archive: project " + accession));
    }

    @Test
    void multipleLinkAndIconPointAtPride() throws URISyntaxException {
        var accession1 = generateRandomPrideExperimentAccession();
        var accession2 = generateRandomPrideExperimentAccession();
        when(baselineExperimentMock.getSecondaryAccessions()).thenReturn(ImmutableSet.of(accession1, accession2));
        var result = subject.get(baselineExperimentMock);

        assertThat(result).hasSize(2);

        assertThat(result)
                .element(0)
                .hasFieldOrPropertyWithValue(
                        "uri",
                        new URI("redirect:https://www.ebi.ac.uk/pride/archive/projects/" + accession1))
                .hasFieldOrPropertyWithValue(
                        "description",
                        ExternallyAvailableContent.Description.create("icon-pride", "PRIDE Archive: project " + accession1));

        assertThat(result)
                .element(1)
                .hasFieldOrPropertyWithValue(
                        "uri",
                        new URI("redirect:https://www.ebi.ac.uk/pride/archive/projects/" + accession2))
                .hasFieldOrPropertyWithValue(
                        "description",
                        ExternallyAvailableContent.Description.create("icon-pride", "PRIDE Archive: project " + accession2));
    }


    @Test
    void noLinkAndIconPointAtPride() throws URISyntaxException {
        when(baselineExperimentMock.getSecondaryAccessions()).thenReturn(ImmutableSet.of());
        assertThat(subject.get(baselineExperimentMock))
                .hasSize(0);

    }

    @Test
    void goesIntoSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
