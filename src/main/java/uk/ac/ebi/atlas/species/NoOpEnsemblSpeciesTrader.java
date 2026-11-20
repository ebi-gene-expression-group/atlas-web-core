package uk.ac.ebi.atlas.species;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile("cli")
@Component
public class NoOpEnsemblSpeciesTrader extends EnsemblSpeciesTrader {
    @Override
    public Optional<String> speciesNameForEnsemblId(String ensemblId) {
        return Optional.empty();
    }
}
