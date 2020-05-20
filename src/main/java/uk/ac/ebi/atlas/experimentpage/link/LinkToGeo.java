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

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public abstract class LinkToGeo<E extends Experiment> extends ExternallyAvailableContent.Supplier<E> {
    private static final UriBuilder GEO_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ncbi.nlm.nih.gov")
                    .pathSegment("geo")
                    .pathSegment("query")
                    .pathSegment("acc.cgi")
                    .queryParam("acc", "{0}");


    private static final WebClient webClient = WebClient.create();

    private static final Function<String, String> formatLabelToGeo =
            arrayAccession -> MessageFormat.format("GEO: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createGeoIcon =
            label -> ExternallyAvailableContent.Description.create("icon-geo", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForGeo =
            formatLabelToGeo.andThen(createGeoIcon);

    @Override
    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    @Component
    public static class ProteomicsBaseline extends LinkToGeo<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            return GenerateResourceLinks.getLinks(experiment, "GSE.*", GEO_URI_BUILDER, createIconForGeo);
        }
    }

    @Component
    public static class RnaSeqBaseline extends LinkToGeo<BaselineExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
            return GenerateResourceLinks.getLinks(experiment, "GSE.*", GEO_URI_BUILDER, createIconForGeo);
        }
    }

    @Component
    public static class Differential extends LinkToGeo<DifferentialExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(DifferentialExperiment experiment) {
            return GenerateResourceLinks.getLinks(experiment, "GSE.*", GEO_URI_BUILDER, createIconForGeo);
        }
    }

    @Component
    public static class Microarray extends LinkToGeo<MicroarrayExperiment> {
        @Override
        public Collection<ExternallyAvailableContent> get(MicroarrayExperiment experiment) {
            return GenerateResourceLinks.getLinks(experiment, "GSE.*", GEO_URI_BUILDER, createIconForGeo);
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
