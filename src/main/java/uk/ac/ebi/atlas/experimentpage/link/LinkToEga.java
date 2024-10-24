package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Map.entry;

@Component
public class LinkToEga {
    private static final WebClient webClient = WebClient.create();

    private static final UriBuilder EGA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ega")
                    .pathSegment("{0}")
                    .pathSegment("{1}");
    private static final Map<String, String> EGA_RESOURCE_TYPE_MAPPING = Map.ofEntries(
            entry("EGAD.*", "datasets"),
            entry("EGAS.*", "studies")
    );

    private static final Function<String, String> formatLabelToEga =
            arrayAccession -> MessageFormat.format("EGA: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createEgaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ega", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForEga =
            formatLabelToEga.andThen(createEgaIcon);

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment experiment) {
        return getLinks(experiment, EGA_RESOURCE_TYPE_MAPPING, EGA_URI_BUILDER, createIconForEga);
    }

    public static ImmutableList<ExternallyAvailableContent> getLinks(Experiment<?> experiment,
                                                                     Map<String, String> resourceTypeMapping,
                                                                     UriBuilder uriBuilder,
                                                                     Function<String, ExternallyAvailableContent.Description> createIcon) {
        if (experiment.getSecondaryAccessions() == null || experiment.getSecondaryAccessions().isEmpty()) {
            return ImmutableList.of();
        }

        return experiment.getSecondaryAccessions().stream()
                .map(accession -> {
                    String EGAPathSegment = resourceTypeMapping.entrySet().stream()
                            .filter(entry -> accession.matches(entry.getKey()))
                            .findFirst()
                            .map(Map.Entry::getValue)
                            .orElse("");
                    var link = uriBuilder.build(EGAPathSegment, accession);
                    return isUriValid(link) ? new ExternallyAvailableContent(link.toString(), createIcon.apply(accession)) : null;
                })
                .filter(Objects::nonNull)
                .collect(ImmutableList.toImmutableList());
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
