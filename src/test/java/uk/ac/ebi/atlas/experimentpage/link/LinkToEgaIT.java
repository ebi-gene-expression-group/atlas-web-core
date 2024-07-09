package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEgaIT {
    LinkToEga subject = new LinkToEga();

    @Test
    void linksIfExperimentIsOnEga() {
        var proteomicsBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(ImmutableList.of("EGA4545", "EGAS4546"))
                        .build();

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
    void linksToEgaShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}