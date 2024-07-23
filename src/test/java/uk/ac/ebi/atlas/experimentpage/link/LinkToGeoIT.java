package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToGeoIT {
    LinkToGeo subject = new LinkToGeo();

    @Test
    void linksIfExperimentIsOnGeo() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE150361", "GSE5454"))
                        .build();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE150361"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE5454"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @Test
    void linksToGeoShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
