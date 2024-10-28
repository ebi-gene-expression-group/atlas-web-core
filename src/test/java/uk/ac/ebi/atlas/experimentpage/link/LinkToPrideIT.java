package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

import static com.google.common.collect.ImmutableList.toImmutableList;
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

    final String PRIDE_URI = "redirect:https://www.ebi.ac.uk/pride/archive/projects/";
    final String PRIDE_DESCRIPTION = "PRIDE Archive: project ";

    @Test
    void givenLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() throws URISyntaxException {
        var secondaryAccessions = List.of(generateRandomPrideExperimentAccession());
        var experiment = new ExperimentBuilder.TestExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            assertResourceLink(resourceLink, secondaryAccessions.get(0));
        }
    }

    @Test
    void givenMultipleLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() throws URISyntaxException {
        Random rand = new Random();

        var secondaryAccessions = rand.ints(20, 0, 9999)
                .mapToObj(index -> "PXD" + index)
                .collect(toImmutableList());

        var experiment = new ExperimentBuilder.TestExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());

        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionPrefixFromLink = link.substring(
                            link.lastIndexOf("/") + 1);
            assertResourceLink(resourceLink, accessionPrefixFromLink);
        }
    }

    @Test
    void whenNoExternalResourceAvailableForExperiment_NoLinksAndIconPointAtPride() {
        when(baselineExperimentMock.getSecondaryAccessions()).thenReturn(ImmutableSet.of());
        assertThat(subject.get(baselineExperimentMock))
                .hasSize(0);
    }

    @Test
    void whenExternalResourceAvailableToPrideExperiment_thenShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

    private void assertResourceLink(ExternallyAvailableContent resourceLink, String accessionPrefixFromLink) throws URISyntaxException {
        assertThat(resourceLink)
                .hasFieldOrPropertyWithValue(
                        "uri",
                        new URI(PRIDE_URI + accessionPrefixFromLink))
                .hasFieldOrPropertyWithValue(
                        "description",
                        ExternallyAvailableContent.Description.create(
                                "icon-pride", PRIDE_DESCRIPTION + accessionPrefixFromLink));
    }
}
