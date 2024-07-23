package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToArrayExpressIT {
    LinkToArrayExpress subject = new LinkToArrayExpress();

    @Test
    void emptyLinkIfExperimentNotOnArrayExpress() {
        var differentialExperiment = new ExperimentBuilder.DifferentialExperimentBuilder().build();
        assertThat(subject.get(differentialExperiment)).isEmpty();
    }

    @Test
    void emptyLinkIfMicroarrayExperimentNotOnArrayExpress() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        // This is what happens when our mock data is too close to real data
                        .withArrayDesigns(ImmutableList.of(ArrayDesign.create(randomAlphanumeric(10))))
                        .build();
        assertThat(subject.get(microarrayExperiment)).isEmpty();
    }

    // Ideally we would pick a microarray experiment using JdbcUtils
    @Test
    void linkIfMicroarrayExperimentIsOnArrayExpress() {
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withExperimentAccession("E-MEXP-1968")
                        .withArrayDesigns(ImmutableList.of(ArrayDesign.create("A-AFFY-45")))
                        .build();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(microarrayExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("/experiments/E-MEXP-1968/"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("/arrays/A-AFFY-45/"))
                .hasSize(2);
    }

    @Test
    void linksToArrayExpressShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}

