package uk.ac.ebi.atlas.experimentpage;

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

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public abstract class LinkToEga<E extends Experiment> extends ExternallyAvailableContent.Supplier<E> {
    private static final UriBuilder EGA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ega")
                    .pathSegment("studies")
                    .pathSegment("{0}")
                    .path("/");
    private static final WebClient webClient = WebClient.create();

    private static final Function<String, String> formatLabelToEga =
            arrayAccession -> MessageFormat.format("EGA: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createEgaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ega", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForEga =
            formatLabelToEga.andThen(createEgaIcon);

    @Override
    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    @Component
    public static class ProteomicsBaseline extends LinkToEga<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            var egaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());

            return egaAccessions.stream()
                            .parallel()
                            .map(accession -> Pair.of(EGA_URI_BUILDER.build(accession), accession))
                            .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                            .map(uriAccession -> new ExternallyAvailableContent(
                                    uriAccession.getLeft().toString(),
                                    createIconForEga.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class RnaSeqBaseline extends LinkToEga<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            var egaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());

            return egaAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(EGA_URI_BUILDER.build(accession), accession))
                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            createIconForEga.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class Differential extends LinkToEga<DifferentialExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(DifferentialExperiment experiment) {
            var egaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());

            return egaAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(EGA_URI_BUILDER.build(accession), accession))
                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            createIconForEga.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class Microarray extends LinkToEga<MicroarrayExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(MicroarrayExperiment experiment) {
            var egaAccessions = experiment.getSecondaryAccessions().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());

            return egaAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(EGA_URI_BUILDER.build(accession), accession))
                    .filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            createIconForEga.apply(uriAccession.getRight())))
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
