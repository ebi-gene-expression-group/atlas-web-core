package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SolrJsonFacetBuilderTest {

    private static final class DummySchemaField extends SchemaField<CollectionProxy> {
        private DummySchemaField(String fieldName) {
            super(fieldName);
        }
    }

    private static final DummySchemaField FIELD1 = new DummySchemaField("field1");
    private static final DummySchemaField FIELD2 = new DummySchemaField("field2");

    @Test
    void facetObjectIsBuiltWithCorrectDefaults() {
        JsonObject jsonFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD1)
                .build();

        assertThat(jsonFacet.keySet()).containsExactly(FIELD1.name());

        JsonObject facetObject = jsonFacet.get(FIELD1.name()).getAsJsonObject();

        assertThat(facetObject.keySet())
                .containsExactly("type", "field", "limit");
        assertThat(facetObject.get("limit").getAsInt())
                .isEqualTo(-1);
        assertThat(facetObject.get("type").getAsString())
                .isEqualTo(SolrFacetType.TERMS.name);
    }

    @Test
    void cannotBuildFacetWithoutField() {
        SolrJsonFacetBuilder jsonFacetBuilder = new SolrJsonFacetBuilder<>()
                .setFacetType(SolrFacetType.QUERY);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> jsonFacetBuilder.build());
    }

    @Test
    void nestedFacetDoesNotHaveWrapperObject() {
        JsonObject jsonSubFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD1)
                .setNestedFacet(true)
                .build();

        assertThat(jsonSubFacet.keySet())
                .containsExactly("type", "field", "limit");
    }

    @Test
    void facetWithSubfacetsIsBuiltCorrectly() {
        ImmutableList<SolrJsonFacetBuilder<CollectionProxy>> subFacets = ImmutableList.of(
                new SolrJsonFacetBuilder<>()
                        .setFacetField(FIELD1)
                        .setNestedFacet(true)
        );

        JsonObject jsonFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD2)
                .addSubFacets(subFacets)
                .build();

        JsonObject facetObject = jsonFacet.get(FIELD2.name()).getAsJsonObject();

        assertThat(facetObject.has("facet"))
                .isTrue();

    }

    @Test
    void setFacetNameChangesWrapperObject() {
        JsonObject jsonFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD1)
                .setFacetName("foo")
                .build();

        assertThat(jsonFacet.keySet()).containsExactly("foo");
    }

    @Test
    void setLimitChangesDefaultValue() {
        JsonObject jsonFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD1)
                .setLimit(1000)
                .build();

        JsonObject facetObject = jsonFacet.get(FIELD1.name()).getAsJsonObject();

        assertThat(facetObject.get("limit").getAsInt())
                .isEqualTo(1000);
    }

    @Test
    void domainFiltersBuildCorrectly() {
        JsonObject jsonFacet = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD1)
                .addDomainFilter(FIELD2, "value2")
                .build();

        JsonObject facetObject = jsonFacet.get(FIELD1.name()).getAsJsonObject();

        assertThat(facetObject.has("domain"))
                .isTrue();

        assertThat(facetObject.get("domain").getAsJsonObject().get("filter").getAsJsonArray().get(0).getAsString())
                .isEqualToIgnoringCase(FIELD2.name() + ":value2");

    }
}
