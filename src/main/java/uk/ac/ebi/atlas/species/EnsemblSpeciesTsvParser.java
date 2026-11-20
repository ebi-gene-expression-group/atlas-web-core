package uk.ac.ebi.atlas.species;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class EnsemblSpeciesTsvParser {

    private static final int PREFIX_COLUMN_INDEX = 0;
    private static final int SPECIES_COLUMN_INDEX = 1;

    private final CSVReader csvReader;

    public EnsemblSpeciesTsvParser(CSVReader csvReader) {
        this.csvReader = csvReader;
    }

    ImmutableMap<String, String> parse() throws IOException {
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            if (line.length <= PREFIX_COLUMN_INDEX) {
                continue;
            }

            String prefix = StringUtils.trimToEmpty(line[PREFIX_COLUMN_INDEX]);
            if (!isValidPrefix(prefix)) {
                continue;
            }

            String speciesName = line.length > SPECIES_COLUMN_INDEX
                    ? StringUtils.trimToEmpty(line[SPECIES_COLUMN_INDEX])
                    : "";
            if (speciesName.isEmpty()) {
                continue;
            }

            mapBuilder.put(prefix, speciesName);
        }

        return mapBuilder.build();
    }

    private static boolean isValidPrefix(String prefix) {
        return prefix.startsWith("ENS") || prefix.startsWith("MGP_");
    }
}
