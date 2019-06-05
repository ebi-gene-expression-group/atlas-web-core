package uk.ac.ebi.atlas.utils;

import com.google.common.net.UrlEscapers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getCustomUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentSetLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesAndExperimentType;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsSummaryImageUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getLinkWithEmptyLabel;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
class UrlHelpersIT {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Mock
    Experiment experimentMock;

    @Test
    void speciesUrl() throws MalformedURLException {
        var species = generateRandomSpecies();

        assertThat(new URL(getExperimentsFilteredBySpeciesUrl(species.getReferenceName())))
                .hasPath("/experiments")
                .hasParameter("species", species.getReferenceName());
    }

    @Test
    void experimentUrl() throws MalformedURLException {
        var experimentAccession = generateRandomExperimentAccession();
        when(experimentMock.getAccession()).thenReturn(experimentAccession);

        assertThat(new URL(getExperimentUrl(experimentMock)))
                .hasPath("/experiments/" + UrlEscapers.urlPathSegmentEscaper().escape(experimentAccession));
    }

    @Test
    void speciesAndTypeUrl() throws MalformedURLException {
        var species = generateRandomSpecies();
        var type = ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)];

        assertThat(new URL(getExperimentsFilteredBySpeciesAndExperimentType(species.getReferenceName(), type.name())))
                .hasPath("/experiments")
                .hasParameter("species", species.getReferenceName())
                .hasParameter("experimentType", type.name());
    }

    @Test
    void imageUrl() throws MalformedURLException {
        var imageFileName = randomAlphabetic(5, 20);

        assertThat(new URL(getExperimentsSummaryImageUrl(imageFileName)))
                .hasPath("/resources/images/experiments-summary/" + imageFileName + ".png");
    }

    @Test
    void customUrl() throws MalformedURLException {
        var path = randomAlphabetic(5, 20);

        assertThat(new URL(getCustomUrl(path)))
                .hasPath("/" + path);
    }

    @Test
    void linkWithEmptyLabel() throws Exception {
        var url = generateRandomUrl();

        assertThat(getLinkWithEmptyLabel(url))
                .extracting("left", "right")
                .contains(Optional.empty(), Optional.of(url));
    }


    @Test
    void experimentSetLink() {
        var keyword = randomAlphabetic(3, 5);

        var result = getExperimentSetLink(keyword);
        assertThat(result.getLeft())
                .isEmpty();
        assertThat(result.getRight())
                .get()
                .asString()
                .contains("?experimentSet=" + keyword);
    }

    @Test
    void experimentLink() {
        var experimentAccession = generateRandomExperimentAccession();
        var label = randomAlphabetic(3, 20);

        var result = getExperimentLink(label, experimentAccession);
        assertThat(result.getLeft())
                .isEqualTo(label);
        assertThat(result.getRight())
                .get()
                .asString()
                .contains("/experiments/" + experimentAccession);
    }

    @Test
    void labelInExperimentLinkCanBeOmitted() {
        var experimentAccession = generateRandomExperimentAccession();

        var result = getExperimentLink(experimentAccession);
        assertThat(result.getLeft())
                .isEmpty();
        assertThat(result.getRight())
                .get()
                .asString()
                .contains("/experiments/" + experimentAccession);
    }
}