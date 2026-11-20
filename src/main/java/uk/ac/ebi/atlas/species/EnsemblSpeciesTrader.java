package uk.ac.ebi.atlas.species;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.utils.CsvReaderFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;

@Profile("!cli")
@Component
public class EnsemblSpeciesTrader {
    // Feature type immediately after the species prefix, then eleven digits and an optional version.
    // GT and FM are gene-tree / protein-family IDs, not species-bearing feature types.
    private static final Pattern FEATURE_AND_DIGITS =
            Pattern.compile("^(?:E|G|P|R|T)\\d{11}(?:\\.\\d+)?$");

    private final ImmutableMap<String, String> prefixToSpecies;

    public EnsemblSpeciesTrader(Path ensemblSpeciesFilePath) {
        try (CSVReader tsvReader = CsvReaderFactory.createForTsv(ensemblSpeciesFilePath.toString())) {
            prefixToSpecies = new EnsemblSpeciesTsvParser(tsvReader).parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected EnsemblSpeciesTrader() {
        this.prefixToSpecies = ImmutableMap.of();
    }

    EnsemblSpeciesTrader(ImmutableMap<String, String> prefixToSpecies) {
        this.prefixToSpecies = prefixToSpecies;
    }

    public Optional<String> speciesNameForEnsemblId(String ensemblId) {
        if (ensemblId == null || ensemblId.isEmpty()) {
            return Optional.empty();
        }

        return prefixToSpecies.keySet().stream()
                .filter(ensemblId::startsWith)
                .filter(prefix -> FEATURE_AND_DIGITS.matcher(ensemblId.substring(prefix.length())).matches())
                .max(Comparator.comparingInt(String::length))
                .map(prefixToSpecies::get);
    }
}
