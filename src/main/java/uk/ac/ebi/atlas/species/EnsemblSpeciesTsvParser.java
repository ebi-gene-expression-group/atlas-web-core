package uk.ac.ebi.atlas.species;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;

public class EnsemblSpeciesTsvParser {

    private static final int PREFIX_COLUMN_INDEX = 0;
    private static final int SPECIES_COLUMN_INDEX = 1;

    private final CSVReader csvReader;

    public EnsemblSpeciesTsvParser(CSVReader csvReader) {
        this.csvReader = csvReader;
    }

    ImmutableMap<String, String> parse() throws IOException {
        // First occurrence wins: the source list can repeat the same prefix for multiple assemblies.
        // Guava ImmutableMap.Builder rejects duplicate keys (even with equal values).
        LinkedHashMap<String, String> prefixToSpecies = new LinkedHashMap<>();
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            if (line.length <= PREFIX_COLUMN_INDEX) {
                continue;
            }

            String prefix = StringUtils.trimToEmpty(line[PREFIX_COLUMN_INDEX]);
            if (!isValidPrefix(prefix) || prefixToSpecies.containsKey(prefix)) {
                continue;
            }

            String speciesName = line.length > SPECIES_COLUMN_INDEX
                    ? StringUtils.trimToEmpty(line[SPECIES_COLUMN_INDEX]).toLowerCase(Locale.ENGLISH)
                    : "";
            if (speciesName.isEmpty()) {
                continue;
            }

            prefixToSpecies.put(prefix, speciesName);
        }

        return ImmutableMap.copyOf(prefixToSpecies);
    }

    private static boolean isValidPrefix(String prefix) {
        return prefix.startsWith("ENS") || prefix.startsWith("MGP_");
    }
}
