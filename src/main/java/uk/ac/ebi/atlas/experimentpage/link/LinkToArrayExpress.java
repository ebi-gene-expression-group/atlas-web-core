package uk.ac.ebi.atlas.experimentpage.link;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.model.experiment.singlecell.SingleCellBaselineExperiment;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class LinkToArrayExpress {
    // You’ll get a 302 if the last slash is missing!
    private static final UriBuilder BIOSTUDIES_API_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("biostudies")
                    .pathSegment("api")
                    .pathSegment("v1")
                    .pathSegment("studies")
                    .pathSegment("{0}");
    // You’ll get a 302 if the last slash is missing!
    private static final UriBuilder EXPERIMENTS_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("arrayexpress")
                    .pathSegment("experiments")
                    .pathSegment("{0}")
                    .path("/");
    private static final UriBuilder ARRAYS_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("arrayexpress")
                    .pathSegment("arrays")
                    .pathSegment("{0}")
                    .path("/");
    private static final WebClient webClient = WebClient.create();

    private static final Function<Experiment, String> formatLabelToExperiment =
            e -> MessageFormat.format("ArrayExpress: experiment {0}", e.getAccession());
    private static final Function<String, String> formatLabelToArray =
            arrayAccession -> MessageFormat.format("ArrayExpress: array design {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ae", label);

    private static final Function<Experiment, ExternallyAvailableContent.Description> createIconForExperiment =
            formatLabelToExperiment.andThen(createIcon);
    private static final Function<String, ExternallyAvailableContent.Description> createIconForArray =
            formatLabelToArray.andThen(createIcon);

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment experiment) {
        var externalLinkFromExperiment = Stream.of(experiment.getAccession())
                .map(accession -> Pair.of(EXPERIMENTS_URI_BUILDER.build(accession), BIOSTUDIES_API_URI_BUILDER.build(accession)))
                .filter(pairOfLinks -> LinkToArrayExpress.isUriValid(pairOfLinks.getRight()))
                .map(Pair::getLeft)
                .map(uri -> new ExternallyAvailableContent(
                        uri.toString(),
                        createIconForExperiment.apply(experiment)));
        if(experiment.getType().isMicroarray()) {
            return Stream.concat(
                            externalLinkFromExperiment,
                            ((MicroarrayExperiment) experiment).getArrayDesignAccessions().stream()
                                    .parallel()
                                    .map(accession -> Pair.of(ARRAYS_URI_BUILDER.build(accession), accession))
                                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                                    .map(uriAccession -> new ExternallyAvailableContent(
                                            uriAccession.getLeft().toString(),
                                            createIconForArray.apply(uriAccession.getRight()))))
                    .collect(toImmutableList());
        }

         return externalLinkFromExperiment.collect(toImmutableList());
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
