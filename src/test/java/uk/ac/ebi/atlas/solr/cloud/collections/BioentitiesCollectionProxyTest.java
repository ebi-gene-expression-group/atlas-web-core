package uk.ac.ebi.atlas.solr.cloud.collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.toDocValues;

class BioentitiesCollectionProxyTest {
    @Test
    void supportedDocValuesFields() {
        assertThat(BioentitiesCollectionProxy.toDocValues(SPECIES)).isNotEqualTo(SPECIES);
        assertThat(BioentitiesCollectionProxy.toDocValues(BIOENTITY_IDENTIFIER)).isNotEqualTo(BIOENTITY_IDENTIFIER);
    }

    @Test
    void unsupportedDocValuesFields() {
        assertThatIllegalArgumentException().isThrownBy(() -> toDocValues(PROPERTY_NAME));
        assertThatIllegalArgumentException().isThrownBy(() -> toDocValues(PROPERTY_VALUE));
    }
}