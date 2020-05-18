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
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public abstract class LinkToGeoEnaEga<E extends Experiment> extends ExternallyAvailableContent.Supplier<E> {
    private static final UriBuilder GEO_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ncbi.nlm.nih.gov")
                    .pathSegment("geo")
                    .pathSegment("query")
                    .pathSegment("acc.cgi?acc={0}")
                    .path("/");
    private static final UriBuilder ENA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ena")
                    .pathSegment("data")
                    .pathSegment("view")
                    .pathSegment("{0}")
                    .path("/");
    private static final UriBuilder EGA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ega")
                    .pathSegment("studies")
                    .pathSegment("{0}")
                    .path("/");
    private static final WebClient webClient = WebClient.create();

    private static final Function<String, String> formatLabelToGeo =
            arrayAccession -> MessageFormat.format("GEO: {0}", arrayAccession);
    private static final Function<String, String> formatLabelToEna =
            arrayAccession -> MessageFormat.format("ENA: {0}", arrayAccession);
    private static final Function<String, String> formatLabelToEga =
            arrayAccession -> MessageFormat.format("EGA: {0}", arrayAccession);


    private static final Function<String, ExternallyAvailableContent.Description> createGeoIcon =
            label -> ExternallyAvailableContent.Description.create("icon-geo", label);
    private static final Function<String, ExternallyAvailableContent.Description> createEgaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ega", label);
    private static final Function<String, ExternallyAvailableContent.Description> createEnaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ena", label);


    private static final Function<String, ExternallyAvailableContent.Description> createIconForGeo =
            formatLabelToGeo.andThen(createGeoIcon);
    private static final Function<String, ExternallyAvailableContent.Description> createIconForEna =
            formatLabelToEna.andThen(createEnaIcon);
    private static final Function<String, ExternallyAvailableContent.Description> createIconForEga =
            formatLabelToEga.andThen(createEgaIcon);


    @Override
    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    @Component
    public static class ProteomicsBaseline extends LinkToGeoEnaEga<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            var geoAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("GSE.*"))
                    .collect(toImmutableList());
            var egaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());
            var enaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            var externalResourceAccessions = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    enaAccessions : egaAccessions : geoAccessions;
            var uriBuilder = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    ENA_URI_BUILDER : EGA_URI_BUILDER : GEO_URI_BUILDER;
            var externalResourceIcon = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    createIconForEna : createIconForEga : createIconForGeo;

            return externalResourceAccessions.stream()
                            .parallel()
                            .map(accession -> Pair.of(uriBuilder.build(accession), accession))
                            //.filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                            .map(uriAccession -> new ExternallyAvailableContent(
                                    uriAccession.getLeft().toString(),
                                    externalResourceIcon.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class RnaSeqBaseline extends LinkToGeoEnaEga<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            var geoAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("GSE.*"))
                    .collect(toImmutableList());
            var egaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());
            var enaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            var externalResourceAccessions = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    enaAccessions : egaAccessions : geoAccessions;

            var uriBuilder = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    ENA_URI_BUILDER : EGA_URI_BUILDER : GEO_URI_BUILDER;
            var externalResourceIcon = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    createIconForEna : createIconForEga : createIconForGeo;

            return externalResourceAccessions.stream()
                            .parallel()
                            .map(accession -> Pair.of(uriBuilder.build(accession), accession))
                            //.filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                            .map(uriAccession -> new ExternallyAvailableContent(
                                    uriAccession.getLeft().toString(),
                                    externalResourceIcon.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class Differential extends LinkToGeoEnaEga<DifferentialExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(DifferentialExperiment experiment) {
            var geoAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("GSE.*"))
                    .collect(toImmutableList());
            var egaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());
            var enaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            var externalResourceAccessions = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    enaAccessions : egaAccessions : geoAccessions;

            var uriBuilder = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    ENA_URI_BUILDER : EGA_URI_BUILDER : GEO_URI_BUILDER;
            var externalResourceIcon = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    createIconForEna : createIconForEga : createIconForGeo;

            return externalResourceAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(uriBuilder.build(accession), accession))
                    //.filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            externalResourceIcon.apply(uriAccession.getRight())))
                    .collect(toImmutableList());
        }
    }

    @Component
    public static class Microarray extends LinkToGeoEnaEga<MicroarrayExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(MicroarrayExperiment experiment) {
            var geoAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("GSE.*"))
                    .collect(toImmutableList());
            var egaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> accession.matches("EGA.*"))
                    .collect(toImmutableList());
            var enaAccessions = experiment.getSecondaryAccession().stream()
                    .filter(accession -> !accession.matches("GSE.*|EGA.*"))
                    .collect(toImmutableList());

            var externalResourceAccessions = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    enaAccessions : egaAccessions : geoAccessions;

            var uriBuilder = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    ENA_URI_BUILDER : EGA_URI_BUILDER : GEO_URI_BUILDER;
            var externalResourceIcon = geoAccessions.isEmpty() ? egaAccessions.isEmpty() ?
                    createIconForEna : createIconForEga : createIconForGeo;

            return externalResourceAccessions.stream()
                    .parallel()
                    .map(accession -> Pair.of(uriBuilder.build(accession), accession))
                    //.filter(uriAccession -> isUriValid(uriAccession.getLeft()))
                    .map(uriAccession -> new ExternallyAvailableContent(
                            uriAccession.getLeft().toString(),
                            externalResourceIcon.apply(uriAccession.getRight())))
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
