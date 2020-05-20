package uk.ac.ebi.atlas.experimentpage;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public abstract class LinkToEna<E extends Experiment> extends ExternallyAvailableContent.Supplier<E> {
    private static final UriBuilder ENA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ena")
                    .pathSegment("data")
                    .pathSegment("view")
                    .pathSegment("{0}")
                    .path("/");

    private static final WebClient webClient = WebClient.create();

    private static final Function<String, String> formatLabelToEna =
            arrayAccession -> MessageFormat.format("ENA: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createEnaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ena", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForEna =
            formatLabelToEna.andThen(createEnaIcon);

    @Override
    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    @Component
    public static class ProteomicsBaseline extends LinkToEna<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            var enaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            return enaAccessions.stream()
                            .parallel()
                            .map(accession -> Pair.of(ENA_URI_BUILDER.build(accession), accession))
                            .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                            .map(uriAccession -> new ExternallyAvailableContent(
                                    uriAccession.getLeft().toString(),
                                    createIconForEna.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class RnaSeqBaseline extends LinkToEna<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            var enaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            return enaAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(ENA_URI_BUILDER.build(accession), accession))
                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            createIconForEna.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class Differential extends LinkToEna<DifferentialExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(DifferentialExperiment experiment) {
            var enaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            return enaAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(ENA_URI_BUILDER.build(accession), accession))
                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            createIconForEna.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class Microarray extends LinkToEna<MicroarrayExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(MicroarrayExperiment experiment) {
            var enaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            return enaAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(ENA_URI_BUILDER.build(accession), accession))
                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            createIconForEna.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
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
