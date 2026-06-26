package uk.ac.ebi.atlas.experimentpage.link;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.configuration.WebClientCliConfig;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceLinkGeneratorTest {

    private static UriBuilder uriBuilder;
    private static Function<String, ExternallyAvailableContent.Description> createIcon;
    Experiment<?> experiment;

    private final ResourceLinkGenerator subject = spy(new ResourceLinkGenerator(new WebClientCliConfig().webClient()));

    @BeforeEach
    void setUp() {
        uriBuilder = mock(UriBuilder.class);
        Function<String, String> formatLabelToArchive =
                accession -> MessageFormat.format("Label text: {0}", accession);
        Function<String, ExternallyAvailableContent.Description> createArchiveIcon =
                label -> ExternallyAvailableContent.Description.create("icon-archive", label);
        createIcon = formatLabelToArchive.andThen(createArchiveIcon);
    }

    @Test
    void whenExperimentHasNoSecondaryAccessions_thenEmptyListReturnedForLinks() {
        experiment = createTestExperiment(List.of());

        var links = subject.getLinks(experiment, egaResourceTypeMapping(), uriBuilder, createIcon);

        assertThat(links).isEmpty();
    }

    @Test
    void whenExperimentHasSecondaryAccessions_thenAListOfLinksReturned() throws URISyntaxException {
        stubUriBuilderAndValidLinks();
        var matchingSecondaryAccessions = List.of("EGAD1234", "EGAS5678");
        experiment = createTestExperiment(matchingSecondaryAccessions);

        var links = subject.getLinks(experiment, egaResourceTypeMapping(), uriBuilder, createIcon);

        assertThat(links).hasSize(matchingSecondaryAccessions.size());
    }

    @Test
    void whenExperimentHasSecondaryAccessions_onlyLinksReturnedThatMatchingGivenResourceTypeMapping()
            throws URISyntaxException {
        stubUriBuilderAndValidLinks();
        var matchingSecondaryAccessions = List.of("EGAD1234", "EGAS5678");
        var secondaryAccessions = Stream.concat(
                        matchingSecondaryAccessions.stream(),
                        Stream.of("GSE5678"))
                .collect(Collectors.toList());
        experiment = createTestExperiment(secondaryAccessions);

        var links = subject.getLinks(experiment, egaResourceTypeMapping(), uriBuilder, createIcon);

        assertThat(links).hasSize(matchingSecondaryAccessions.size());
    }

    @Test
    void whenURIisInvalid_thenReturnsFalse() throws URISyntaxException {
        assertIsUriValid(new URI("https://__notvalid___example.com/foo"), false);
    }

    @Test
    void whenURIisValid_thenReturnsTrue() throws Exception {
        try (var http = localHttpServer(200)) {
            assertIsUriValid(http.baseUri(), true);
        }
    }

    private void stubUriBuilderAndValidLinks() throws URISyntaxException {
        when(uriBuilder.build(any(), any())).thenReturn(new URI("https://example.org/foo"));
        doReturn(true).when(subject).isUriValid(any());
    }

    private static Map<String, String> egaResourceTypeMapping() {
        return Map.ofEntries(
                entry("EGAD.*", "datasets"),
                entry("EGAS.*", "studies")
        );
    }

    /** Real implementation — not the spy used by getLinks tests. */
    private static void assertIsUriValid(URI uri, boolean expected) {
        assertThat(uriValidationSubject().isUriValid(uri)).isEqualTo(expected);
    }

    private static ResourceLinkGenerator uriValidationSubject() {
        return new ResourceLinkGenerator(new WebClientCliConfig().webClient());
    }

    private static LocalHttpServer localHttpServer(int statusCode) throws IOException {
        return new LocalHttpServer(statusCode);
    }

    private static ExperimentTest.TestExperiment createTestExperiment(List<String> secondaryAccessions) {
        return new ExperimentBuilder.TestExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();
    }

    private static final class LocalHttpServer implements AutoCloseable {
        private final HttpServer server;

        private LocalHttpServer(int statusCode) throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                exchange.sendResponseHeaders(statusCode, -1);
                exchange.close();
            });
            server.start();
        }

        private URI baseUri() throws URISyntaxException {
            return new URI("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
