package uk.ac.ebi.atlas.species;

import org.springframework.context.annotation.Profile;
import uk.ac.ebi.atlas.utils.EnsemblLookupClient;

import javax.inject.Named;
import java.util.Optional;

@Named
@Profile("cli")
public class NoOpEnsemblLookupClient extends EnsemblLookupClient {
    @Override
    public Optional<Species> lookupSpecies(String ensemblId) {
        return Optional.empty();
    }
}
