package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEnaIT {
    LinkToEna subject = new LinkToEna();


    @Test
    void linksIfExperimentIsOnEna() {
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("ERP4545", "ERP4546"))
                        .build();

        // We can’t use URI::getPath because the redirect prefix messes it up :/
        assertThat(subject.get(RnaSeqBaselineExperiment))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4545"))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith("ERP4546"))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type().equals("icon-ena"))
                .hasSize(2);
    }

    @Test
    void linksToEnaShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}