package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEgaIT {

    String EXPECTED_DESCRIPTION_TYPE = "icon-ega";

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
                .hasSize(secondaryAccessions.size())
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(egaDataSetAccession))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(egaStudyAccession))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type()
                        .equals(EXPECTED_DESCRIPTION_TYPE));
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

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            assertThat(resourceLink.uri.toString()).endsWith(expectedURLEnding);
        }
    }

    @Test
    void givenExperimentHasMoreThan1EGADatasetResources_ThenAvailableResourcesContainsCorrectEGADatasetLinks() {
        var egaDataSetAccession1 = "EGAD4545";
        var egaDataSetAccession2 = "EGAD1234";
        var secondaryAccessions = ImmutableList.of(egaDataSetAccession1, egaDataSetAccession2);
        var experiment = new ExperimentBuilder.BaselineExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();
        var expectedURLRegexp = ".*/ega/datasets/EGAD.*";

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            assertThat(resourceLink.uri.toString()).matches(expectedURLRegexp);
        }
    }

    @Test
    void givenExperimentHasDifferentEGAResources_ThenAvailableResourcesContainsCorrectEGAResourceLinks() {
        Random rand = new Random();

        var secondaryAccessions = Stream.generate(() -> rand.nextBoolean() ? "D" : "S")
                        .limit(20)
                        .map(type -> "EGA" + type + rand.nextInt())
                        .collect(toImmutableList());
        var linkTypes = Map.ofEntries(
                entry("EGAD", ".*/ega/datasets/"),
                entry("EGAS", ".*/ega/studies/")
        );
        var experiment = new ExperimentBuilder.BaselineExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionPrefixFromLink = link.substring(link.lastIndexOf("/") + 1)
                    .substring(0, 4);
            var pathSegmentType = linkTypes.get(accessionPrefixFromLink);
            var expectedURLRegexp = pathSegmentType + accessionPrefixFromLink + ".*";
            assertThat(link).matches(expectedURLRegexp);
            assertThat(resourceLink.description.type()).isEqualTo(EXPECTED_DESCRIPTION_TYPE);
        }
    }

    @Test
    void linksToEgaShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}