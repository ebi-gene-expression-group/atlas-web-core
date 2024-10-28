package uk.ac.ebi.atlas.experimentpage.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToGeoIT {

    String EXPECTED_DESCRIPTION_TYPE = "icon-geo";

    LinkToGeo subject;

    @BeforeEach
    void setUp() {
        subject = new LinkToGeo();
    }

    @Test
    void givenLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() {
        var secondaryAccessions = List.of("GSE150361", "GSE5454");
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(secondaryAccessions)
                        .build();

        assertThat(subject.get(differentialExperiment))
                .hasSize(secondaryAccessions.size())
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(secondaryAccessions.get(0)))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(secondaryAccessions.get(1)))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type()
                        .equals(EXPECTED_DESCRIPTION_TYPE));
    }

    @Test
    void givenExperimentHasDifferentGEOResources_ThenAvailableResourcesContainsCorrectGEOResourceLinks() {
        Random rand = new Random();

        final String accessionParamName = "acc=";

        var secondaryAccessions = Stream.generate(() -> rand.nextBoolean() ? "GSE" : "GDS")
                .limit(20)
                .map(type -> type + rand.nextInt())
                .collect(toImmutableList());
        var linkTypes = Map.ofEntries(
                entry("GSE", ".*/geo/query/acc.cgi\\?acc="),
                entry("GDS", ".*/geo/query/acc.cgi\\?acc=")
        );
        var experiment = new ExperimentBuilder.BaselineExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionPrefixFromLink = link.substring(
                    link.lastIndexOf(accessionParamName) + accessionParamName.length())
                    .substring(0, 3);
            var pathSegmentType = linkTypes.get(accessionPrefixFromLink);
            var expectedURLRegexp = pathSegmentType + accessionPrefixFromLink + ".*";
            assertThat(link).matches(expectedURLRegexp);
            assertThat(resourceLink.description.type()).isEqualTo(EXPECTED_DESCRIPTION_TYPE);
        }
    }

    @Test
    void linksToGeoShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
