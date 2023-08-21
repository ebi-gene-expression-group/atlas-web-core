
package uk.ac.ebi.atlas.trader;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value(staticConstructor = "of")
public final class ExperimentDesignData {
    private final Map<String, List<String>> characteristics;
    private final Map<String, List<String>> factorValues;
    private final Map<String, List<String>> arrayDesigns;

    public Map<String, List<String>> getCharacteristics() {
        return this.characteristics;
    }

    public Map<String, List<String>> getFactorValues() {
        return this.factorValues;
    }

    public Map<String, List<String>> getArrayDesigns() {
        return this.arrayDesigns;
    }
}