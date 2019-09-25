package uk.ac.ebi.atlas.experimentimport.sdrf;

import java.util.List;
import java.util.Optional;

public class SdrfParserOutput {
    private final Optional<List<String>> technologyType;

    public SdrfParserOutput(Optional<List<String>>  technologyType) {
        this.technologyType = technologyType;
    }

    public Optional<List<String>> getTechnologyType() {
        return technologyType;
    }

}
