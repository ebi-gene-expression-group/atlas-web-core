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
    void iconAndLinkPointAtPride() throws URISyntaxException {
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
    void goesIntoSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
