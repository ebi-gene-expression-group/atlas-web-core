package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToGeoIT {

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfExperimentIsOnGeo() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE150361", "GSE5454"))
                        .build();
        var subject = new LinkToGeo();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE150361"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE5454"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksToGeoShowInSupplementaryInformationTab() {
        assertThat(new LinkToGeo().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
