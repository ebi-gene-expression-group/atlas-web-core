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
public abstract class LinkToEna {
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

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment experiment) {
        return GenerateResourceLinks.getLinks(experiment, "[^G]*", ENA_URI_BUILDER, createIconForEna);
    }

}
