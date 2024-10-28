package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class GenerateResourceLinks {
    private static final WebClient webClient = WebClient.create();

    public static ImmutableList<ExternallyAvailableContent> getLinks(Experiment<?> experiment,
                                                                     Map<String, String> resourceTypeMapping,
                                                                     UriBuilder uriBuilder,
                                                                     Function<String, ExternallyAvailableContent.Description> createIcon) {
        if (noSecondaryAccession(experiment)) {
            return ImmutableList.of();
        }

        return experiment.getSecondaryAccessions().stream()
                .map(accession -> {
                    var link = uriBuilder.build(getPathSegment(resourceTypeMapping, accession), accession);
                    return isUriValid(link) ?
                            new ExternallyAvailableContent(link.toString(), createIcon.apply(accession)) :
                            null;
                })
                .filter(Objects::nonNull)
                .collect(ImmutableList.toImmutableList());
    }

    private static boolean noSecondaryAccession(Experiment<?> experiment) {
        return experiment.getSecondaryAccessions() == null || experiment.getSecondaryAccessions().isEmpty();
    }

    private static String getPathSegment(Map<String, String> resourceTypeMapping, String accession) {
        return resourceTypeMapping.entrySet().stream()
                .filter(entry -> accession.matches(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse("");
    }

    private static boolean isUriValid(@NotNull URI uri) {
        var response = webClient
                .get()
                .uri(uri)
                .exchange()
                .block();
        return response != null && !response.statusCode().isError();
    }
}
