package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import io.github.artsok.RepeatedIfExceptionsTest;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEnaIT {

    @RepeatedIfExceptionsTest(repeats = 5)
    void linksIfExperimentIsOnEna() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "ERP4546"))
                        .build();
        var subject = new LinkToEna();

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
    void linksToEnaShowInSupplementaryInformationTab() {
        assertThat(new LinkToEga().contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}