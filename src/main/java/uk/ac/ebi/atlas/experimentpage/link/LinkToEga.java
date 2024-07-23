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
public class LinkToEga {
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

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment experiment) {
        return GenerateResourceLinks.getLinks(experiment, "EGA.*", EGA_URI_BUILDER, createIconForEga);
    }

}
