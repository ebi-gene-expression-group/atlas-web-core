package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEgaIT {
    LinkToEga subject;

    @BeforeEach
    void setUp() {
        subject = new LinkToEga();
    }

    @Test
    void givenLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() {
        var egaDataSetAccession = "EGAD4545";
        var egaStudyAccession = "EGAS4546";
        var secondaryAccessions = ImmutableList.of(egaDataSetAccession, egaStudyAccession);
        var experiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(secondaryAccessions)
                        .build();

        assertThat(subject.get(experiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(egaDataSetAccession))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(egaStudyAccession))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(secondaryAccessions.size());
    }

    @Test
    void givenExperimentHasEGADatasetResource_ThenAvailableResourcesContainsCorrectEGADatasetLink() {
        var egaDataSetAccession = "EGAD4545";
        var secondaryAccessions = ImmutableList.of(egaDataSetAccession);
        var experiment = new ExperimentBuilder.BaselineExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();
        var expectedURLEnding = "/ega/datasets/" + egaDataSetAccession;

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(1);
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            assertThat(resourceLink.uri.toString()).endsWith(expectedURLEnding);
        }
    }

    @Test
    void linksToEgaShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}