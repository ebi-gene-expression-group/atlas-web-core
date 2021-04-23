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
import java.util.UUID;
import java.util.function.Function;

@Component
public abstract class LinkToEna<E extends Experiment> extends ExternallyAvailableContent.Supplier<E> {
    private static final UriBuilder ENA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ena")
                    .pathSegment("data")
                    .pathSegment("view")
                    .pathSegment("{0}");

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

    @Override
    public Collection<ExternallyAvailableContent> get(E experiment) {
        return GenerateResourceLinks.getLinks(experiment, "[SDE]RP\\d+", ENA_URI_BUILDER, createIconForEna);
    }

    @Component
    public static class ProteomicsBaseline extends LinkToEna<BaselineExperiment> {}

    @Component
    public static class RnaSeqBaseline extends LinkToEna<BaselineExperiment> {}

    @Component
    public static class Differential extends LinkToEna<DifferentialExperiment> {}

    @Component
    public static class Microarray extends LinkToEna<MicroarrayExperiment> {}

    @Component
    public static class SingleCell extends LinkToEna<SingleCellBaselineExperiment> {}
}
