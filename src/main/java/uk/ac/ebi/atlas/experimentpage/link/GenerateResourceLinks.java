package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;

import java.net.URI;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class GenerateResourceLinks {
    private static final WebClient webClient = WebClient.create();

    public static ImmutableList<ExternallyAvailableContent> getLinks(Experiment<?> experiment,
                                                                     String regex,
                                                                     UriBuilder uriBuilder,
                                                                     Function<String, ExternallyAvailableContent.Description> createIcon) {
        return experiment.getSecondaryAccessions().stream()
                .filter(accession -> accession.matches(regex))
                .parallel()
                .map(accession -> Pair.of(uriBuilder.build(accession), accession))
                .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                .map(uriAccession -> new ExternallyAvailableContent(
                        uriAccession.getLeft().toString(),
                        createIcon.apply(uriAccession.getRight())))
                .collect(toImmutableList());
    }

    private static boolean isUriValid(@NotNull URI uri) {
        try {
            return !webClient
                    .get()
                    .uri(uri)
                    .exchange()
                    .block()
                    .statusCode()
                    .isError();
        } catch (Exception e) {
            return false;
        }
    }
}
