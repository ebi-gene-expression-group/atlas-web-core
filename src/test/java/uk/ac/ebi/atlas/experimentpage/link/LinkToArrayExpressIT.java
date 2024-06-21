package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToArrayExpressIT {

    @Test
    void emptyLinkIfExperimentNotOnArrayExpress() {
        var differentialExperiment = new ExperimentBuilder.DifferentialExperimentBuilder().build();
        var subject = new LinkToArrayExpress();

        assertThat(subject.get(differentialExperiment)).isEmpty();
    }

    @Test
    void emptyLinkIfMicroarrayExperimentNotOnArrayExpress() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        // This is what happens when our mock data is too close to real data
                        .withArrayDesigns(ImmutableList.of(ArrayDesign.create(randomAlphanumeric(10))))
                        .build();
        var subject = new LinkToArrayExpress();

        assertThat(subject.get(microarrayExperiment)).isEmpty();
    }

    // Ideally we would pick a microarray experiment using JdbcUtils
    @RepeatedIfExceptionsTest(repeats = 5)
    void linkIfMicroarrayExperimentIsOnArrayExpress() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withExperimentAccession("E-MEXP-1968")
                        .withArrayDesigns(ImmutableList.of(ArrayDesign.create("A-AFFY-45")))
                        .build();
        var subject = new LinkToArrayExpress();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("/experiments/E-MEXP-1968/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("/arrays/A-AFFY-45/"))
                .hasSize(2);
    }

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksToArrayExpressShowInSupplementaryInformationTab() {
        assertThat(new LinkToArrayExpress().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}

