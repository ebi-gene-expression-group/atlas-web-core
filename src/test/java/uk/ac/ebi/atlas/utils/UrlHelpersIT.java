package uk.ac.ebi.atlas.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getCustomUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentSetLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesAndExperimentType;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsSummaryImageUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getLinkWithEmptyLabel;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
class UrlHelpersIT {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void utilityClass() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(UrlHelpers::new);
    }

    @Test
    void speciesUrl() throws Exception {
        var species = generateRandomSpecies();

        assertThat(new URL(getExperimentsFilteredBySpeciesUrl(species.getReferenceName())))
                .hasPath("/experiments")
                .hasParameter("species", species.getReferenceName());
    }

    @Test
    void speciesAndTypeUrl() throws Exception {
        var species = generateRandomSpecies();
        var type = ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)];

        assertThat(new URL(getExperimentsFilteredBySpeciesAndExperimentType(species.getReferenceName(), type.name())))
                .hasPath("/experiments")
                .hasParameter("species", species.getReferenceName())
                .hasParameter("experimentType", type.name());
    }

    @Test
    void imageUrl() throws Exception {
        var imageFileName = randomAlphabetic(5, 20);

        assertThat(new URL(getExperimentsSummaryImageUrl(imageFileName)))
                .hasPath("/resources/images/experiments-summary/" + imageFileName + ".png");
    }

    @Test
    void customUrl() throws Exception {
        var path = "/" + randomAlphabetic(5, 20);

        assertThat(new URL(getCustomUrl(path)))
                .hasPath(path);
    }

    @Test
    void fullyQualifiedCustomUrl() throws Exception {
        var path = "/" + randomAlphabetic(5, 20);
        var host = new URL(generateRandomUrl()).getHost();

        assertThat(new URL(getCustomUrl(host, path)))
                .hasProtocol("https")
                .hasHost(host)
                .hasPath(path);
    }

    @Test
    void linkWithEmptyLabel() throws Exception {
        var url = generateRandomUrl();

        assertThat(getLinkWithEmptyLabel(url))
                .extracting("left", "right")
                .contains(Optional.empty(), Optional.of(url));
    }


    @Test
    void experimentSetLink() throws Exception {
        var keyword = randomAlphabetic(3, 5);

        var result = getExperimentSetLink(keyword);
        assertThat(result.getLeft())
                .isEmpty();
        assertThat(new URL(result.getRight().get()))
                .hasParameter("experimentSet", keyword);
    }

    @Test
    void experimentLink() throws Exception {
        var experimentAccession = generateRandomExperimentAccession();
        var label = randomAlphabetic(3, 20);

        var result = getExperimentLink(label, experimentAccession);
        assertThat(result.getLeft())
                .isEqualTo(label);
        assertThat(new URL(result.getRight().get()))
                .hasPath("/experiments/" + experimentAccession);
    }

    @Test
    void fullyQualifiedExperimentLink() throws Exception {
        var experimentAccession = generateRandomExperimentAccession();
        var label = randomAlphabetic(3, 20);
        var host = new URL(generateRandomUrl()).getHost();

        var result = getExperimentLink(host, label, experimentAccession);
        assertThat(result.getLeft())
                .isEqualTo(label);
        assertThat(new URL(result.getRight().get()))
                .hasProtocol("https")
                .hasHost(host)
                .hasPath("/experiments/" + experimentAccession);
    }

    @Test
    void labelInExperimentLinkCanBeOmitted() throws Exception {
        var experimentAccession = generateRandomExperimentAccession();

        var result = getExperimentLink(experimentAccession);
        assertThat(result.getLeft())
                .isEmpty();
        assertThat(new URL(result.getRight().get()))
                .hasPath("/experiments/" + experimentAccession);
    }
}