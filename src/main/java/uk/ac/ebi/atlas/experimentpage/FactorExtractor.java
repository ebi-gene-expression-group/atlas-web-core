package uk.ac.ebi.atlas.experimentpage;

import com.google.common.collect.ImmutableSet;

public interface FactorExtractor {
    ImmutableSet<String> getFactorHeaders(String experimentAccession);
}
