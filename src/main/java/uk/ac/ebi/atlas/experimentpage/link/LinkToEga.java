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
public abstract class LinkToEga<E extends Experiment> extends ExternallyAvailableContent.Supplier<E> {
    private static final UriBuilder EGA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ega")
                    .pathSegment("studies")
                    .pathSegment("{0}")
                    .path("/");

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

    @Override
    public Collection<ExternallyAvailableContent> get(E experiment) {
        return GenerateResourceLinks.getLinks(experiment, "EGA.*", EGA_URI_BUILDER, createIconForEga);
    }

    @Component
    public static class ProteomicsBaseline extends LinkToEga<BaselineExperiment> {}

    @Component
    public static class RnaSeqBaseline extends LinkToEga<BaselineExperiment> {}

    @Component
    public static class Differential extends LinkToEga<DifferentialExperiment> {}

    @Component
    public static class Microarray extends LinkToEga<MicroarrayExperiment> {}

    @Component
    public static class SingleCell extends LinkToEga<SingleCellBaselineExperiment> {}
}
