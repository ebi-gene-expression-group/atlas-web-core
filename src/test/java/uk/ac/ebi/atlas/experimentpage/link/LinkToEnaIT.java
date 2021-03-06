package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEnaIT {
    @RepeatedIfExceptionsTest(repeats = 5)
    void linkIfMicroarrayExperimentIsOnEna() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545"))
                        .build();
        var subject = new LinkToEna.Microarray();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(1);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfProteomicsBaselineExperimentIsOnEga() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "ERP4546"))
                        .build();
        var subject = new LinkToEna.ProteomicsBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(proteomicsBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4546"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfPRnaSeqBaselineExperimentIsOnEna() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "ERP4546"))
                        .build();
        var subject = new LinkToEna.RnaSeqBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4546"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfDifferentialExperimentIsOnEna() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545"))
                        .build();
        var subject = new LinkToEna.Differential();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(1);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksToEnaShowInSupplementaryInformationTab() {
        assertThat(new LinkToEga.RnaSeqBaseline().contentType())
                .isEqualTo(new LinkToEna.ProteomicsBaseline().contentType())
                .isEqualTo(new LinkToEna.Differential().contentType())
                .isEqualTo(new LinkToEna.Microarray().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}