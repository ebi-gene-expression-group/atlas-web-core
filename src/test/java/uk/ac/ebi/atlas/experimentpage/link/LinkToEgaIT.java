package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEgaIT {
    @RepeatedIfExceptionsTest(repeats = 5)
    void linkIfMicroarrayExperimentIsOnEga() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545"))
                        .build();
        var subject = new LinkToEga.Microarray();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(1);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfProteomicsBaselineExperimentIsOnEga() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546"))
                        .build();
        var subject = new LinkToEga.ProteomicsBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(proteomicsBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGAS4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfPRnaSeqBaselineExperimentIsOnEga() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546"))
                        .build();
        var subject = new LinkToEga.RnaSeqBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGAS4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfDifferentialExperimentIsOnEga() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546"))
                        .build();
        var subject = new LinkToEga.Differential();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGAS4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksToEgaShowInSupplementaryInformationTab() {
        assertThat(new LinkToEga.RnaSeqBaseline().contentType())
                .isEqualTo(new LinkToEga.ProteomicsBaseline().contentType())
                .isEqualTo(new LinkToEga.Differential().contentType())
                .isEqualTo(new LinkToEga.Microarray().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}