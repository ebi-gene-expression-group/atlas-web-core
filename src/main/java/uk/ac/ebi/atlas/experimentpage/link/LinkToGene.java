package uk.ac.ebi.atlas.experimentpage.link;

import uk.ac.ebi.atlas.model.Profile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

public class LinkToGene<P extends Profile> implements Function<P, URI> {
    @Override
    public URI apply(P profile) {
        try {
            return new URI("genes/" + profile.getId());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
