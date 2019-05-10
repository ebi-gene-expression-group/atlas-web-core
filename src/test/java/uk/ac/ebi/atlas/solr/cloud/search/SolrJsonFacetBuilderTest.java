package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

public class SolrJsonFacetBuilderTest {

    private static final class DummySchemaField extends SchemaField<CollectionProxy> {
        private DummySchemaField(String fieldName) {
            super(fieldName);
        }
    }

    private static final DummySchemaField EXPERIMENT_ACCESSION = new DummySchemaField("experiment_accession");
    private static final DummySchemaField CHARACTERISTIC_NAME = new DummySchemaField("characteristic_name");
    private static final DummySchemaField CHARACTERISTIC_VALUE = new DummySchemaField("characteristic_value");


    @Test
    void testHello() {
        SolrJsonFacetBuilder<CollectionProxy> characteristicValueFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(CHARACTERISTIC_VALUE)
                .setNestedFacet(true);

        SolrJsonFacetBuilder<CollectionProxy> characteristicNameFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(CHARACTERISTIC_NAME)
                .addSubFacets(ImmutableList.of(characteristicValueFacet))
                .setNestedFacet(true);

        SolrJsonFacetBuilder<CollectionProxy> boo = new SolrJsonFacetBuilder<>()
                .setFacetType(SolrFacetType.QUERY);

        JsonObject experimentFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(EXPERIMENT_ACCESSION)
                .addSubFacets(ImmutableList.of(characteristicNameFacet))
                .build();
    }
}
