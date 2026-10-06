package uk.ac.ebi.atlas.model.resource;

import java.nio.file.Path;
import java.text.MessageFormat;

public class HtmlFile extends AtlasResource<Path> {
    public HtmlFile(Path parentDirectory, String template, String... args) {
        super(parentDirectory.resolve(MessageFormat.format(template, (Object[]) args)));
    }

    @Override
    public Path get() {
        return path;
    }
}
