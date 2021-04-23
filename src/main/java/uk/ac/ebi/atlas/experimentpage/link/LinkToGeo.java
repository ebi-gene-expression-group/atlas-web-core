package uk.ac.ebi.atlas.experimentpage.link;

import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.model.experiment.singlecell.SingleCellBaselineExperiment;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;

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

    @Override
    public Collection<ExternallyAvailableContent> get(E experiment) {
        return GenerateResourceLinks.getLinks(experiment, "GSE.*", GEO_URI_BUILDER, createIconForGeo);
    }

    @Component
    public static class ProteomicsBaseline extends LinkToGeo<BaselineExperiment> {}

    @Component
    public static class RnaSeqBaseline extends LinkToGeo<BaselineExperiment> {}

    @Component
    public static class Differential extends LinkToGeo<DifferentialExperiment> {}

    @Component
    public static class Microarray extends LinkToGeo<MicroarrayExperiment> {}

    @Component
    public static class SingleCell extends LinkToGeo<SingleCellBaselineExperiment> {}
}
