package uk.ac.ebi.atlas.model.experiment.summary;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public class AssayGroupSummaryBuilder {
    private ExperimentDesign experimentDesign;
    private AssayGroup assayGroup;
    private Set<AssayProperty> properties = new HashSet<>();

    public AssayGroupSummaryBuilder withExperimentDesign(ExperimentDesign experimentDesign) {
        this.experimentDesign = experimentDesign;
        return this;
    }

    public AssayGroupSummaryBuilder forAssayGroup(AssayGroup assayGroup) {
        this.assayGroup = assayGroup;
        return this;
    }

    public AssayGroupSummary build() {
        checkState(assayGroup != null && experimentDesign != null);

        Multimap<String, String> allFactorValues = HashMultimap.create();
        Multimap<String, String> allSampleValues = HashMultimap.create();
        for (String assay : assayGroup.getAssayIds()) {
            extractAllValues(experimentDesign.getFactorValues(assay), allFactorValues);
            extractAllValues(experimentDesign.getSampleCharacteristicsValues(assay), allSampleValues);
        }

        addAssayProperties(allFactorValues, ContrastPropertyType.FACTOR);
        addAssayProperties(allSampleValues, ContrastPropertyType.SAMPLE);

        return new AssayGroupSummary(assayGroup.getAssays().size(), Sets.newTreeSet(properties));
    }

    private void addAssayProperties(Multimap<String, String> allValues, ContrastPropertyType contrastPropertyType) {
        for (String name : allValues.keySet()) {

            AssayProperty property = new AssayProperty(name,
                    Joiner.on(",").skipNulls().join(allValues.get(name)),
                    contrastPropertyType);

            properties.add(property);
        }
    }

    private void extractAllValues(Map<String, String> samples, Multimap<String, String> allValues) {
        for (String name : samples.keySet()) {
            allValues.put(name, samples.get(name));
        }
    }
}

