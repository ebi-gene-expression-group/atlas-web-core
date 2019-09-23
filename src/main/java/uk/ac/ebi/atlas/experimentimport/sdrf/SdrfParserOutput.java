package uk.ac.ebi.atlas.experimentimport.sdrf;

import java.util.List;

public class SdrfParserOutput {
    private final List<String> technologyType;

    public SdrfParserOutput(List<String>  technologyType) {
        this.technologyType = technologyType;
    }

    public List<String> getTechnologyType() {
        return technologyType;
    }

}
