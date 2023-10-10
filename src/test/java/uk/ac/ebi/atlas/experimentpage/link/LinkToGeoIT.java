package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.ac.ebi.atlas.testutils.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToGeoIT {
    @RepeatedIfExceptionsTest(repeats = 5)
    void linkIfMicroarrayExperimentIsOnGeo() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE150361"))
                        .build();
        var subject = new LinkToGeo.Microarray();

       // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE150361"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(1);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfProteomicsBaselineExperimentIsOnGeo() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE150361", "GSE5656"))
                        .build();
        var subject = new LinkToGeo.ProteomicsBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(proteomicsBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE150361"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE5656"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfRnaSeqBaselineExperimentIsOnGeo() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE150361", "GSE5656"))
                        .build();
        var subject = new LinkToGeo.RnaSeqBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE150361"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE5656"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfDifferentialExperimentIsOnGeo() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE150361", "GSE5454"))
                        .build();
        var subject = new LinkToGeo.Differential();

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
        assertThat(new LinkToGeo.RnaSeqBaseline().contentType())
                .isEqualTo(new LinkToGeo.ProteomicsBaseline().contentType())
                .isEqualTo(new LinkToGeo.Differential().contentType())
                .isEqualTo(new LinkToGeo.Microarray().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
