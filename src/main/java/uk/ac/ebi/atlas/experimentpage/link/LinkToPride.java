package uk.ac.ebi.atlas.experimentpage.link;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;

import static java.util.Collections.singleton;

@Component
public class LinkToPride extends ExternallyAvailableContent.Supplier<BaselineExperiment> {
    private static final Function<BaselineExperiment, String> formatLabel =
            e -> MessageFormat.format("PRIDE Archive: project {0}", e.getSecondaryAccessions().stream().findAny().get());
    private static final Function<BaselineExperiment, String> formatLink =
            e -> MessageFormat.format("https://www.ebi.ac.uk/pride/archive/projects/{0}", e.getSecondaryAccessions().stream().findAny().get());

    private static final Function<BaselineExperiment, ExternallyAvailableContent.Description> createIcon =
            formatLabel.andThen(label -> ExternallyAvailableContent.Description.create("icon-pride", label));

    @Override
    public Collection<ExternallyAvailableContent> get(BaselineExperiment experiment) {
        return singleton(new ExternallyAvailableContent(formatLink.apply(experiment), createIcon.apply(experiment)));
    }

    @Override
    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }
}
