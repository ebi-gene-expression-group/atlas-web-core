package uk.ac.ebi.atlas.experimentpage;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToGeoEnaEgaTest {

    // Ideally we would pick a microarray experiment using JdbcUtils, but LinkToGeoEnaEga takes an experiment object
    // as an argument, and we’d need an experiment trader, which currently are on the parent projects
    @Test
    void linkIfMicroarrayExperimentIsOnGeo() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.Microarray();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4545/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(1);
    }

    @Test
    void linkIfMicroarrayExperimentIsOnEna() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.Microarray();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(1);
    }

    @Test
    void linkIfMicroarrayExperimentIsOnEga() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.Microarray();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(1);
    }

    @Test
    void linksIfProteomicsBaselineExperimentAreOnGeo() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE4545", "GSE4546"))
                        .build();
        var subject = new LinkToGeoEnaEga.ProteomicsBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(proteomicsBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @Test
    void linksIfProteomicsBaselineExperimentAreOnEna() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "SRP4546"))
                        .build();
        var subject = new LinkToGeoEnaEga.ProteomicsBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(proteomicsBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("SRP4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(2);
    }

    @Test
    void linksIfProteomicsBaselineExperimentAreOnEga() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546"))
                        .build();
        var subject = new LinkToGeoEnaEga.ProteomicsBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(proteomicsBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGAS4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(2);
    }

    @Test
    void linksIfRnaSeqBaselineExperimentAreOnGeoAndEna() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE4545", "GSE4546", "ERP4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.RnaSeqBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @Test
    void linksIfRnaSeqBaselineExperimentAreOnEna() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "SRP4546"))
                        .build();
        var subject = new LinkToGeoEnaEga.RnaSeqBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("SRP4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(2);
    }

    @Test
    void linksIfPRnaSeqBaselineExperimentAreOnEgaAndEna() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546", "ERP4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.RnaSeqBaseline();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGAS4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(2);
    }

    @Test
    void linksIfDifferentialExperimentAreOnGeo() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("GSE4545", "GSE4546", "ERP4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.Differential();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("GSE4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-geo"))
                .hasSize(2);
    }

    @Test
    void linkIfDifferentialExperimentIsOnEna() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "SRP4546"))
                        .build();
        var subject = new LinkToGeoEnaEga.Differential();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("SRP4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(2);
    }

    @Test
    void linkIfDifferentialExperimentIsOnEga() {
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546", "ERP4545"))
                        .build();
        var subject = new LinkToGeoEnaEga.Differential();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(differentialExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGA4545/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("EGAS4546/"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ega"))
                .hasSize(2);
    }

    @Test
    void linksToArrayExpressShowInSupplementaryInformationTab() {
        assertThat(new LinkToGeoEnaEga.RnaSeqBaseline().contentType())
                .isEqualTo(new LinkToGeoEnaEga.ProteomicsBaseline().contentType())
                .isEqualTo(new LinkToGeoEnaEga.Differential().contentType())
                .isEqualTo(new LinkToGeoEnaEga.Microarray().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}