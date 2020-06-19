package uk.ac.ebi.atlas.solr.cloud.search;

import org.apache.solr.common.util.SimpleOrderedMap;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SolrQueryResponseUtilsTest {
    @Test
    void utilityClassCannotBeInstantiated() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(SolrQueryResponseUtils::new);
    }

    @Test
    void extractingMapsFromArrayListReturnsListOfMaps() {
        var listOfMaps = new ArrayList<>();

        listOfMaps.add(new SimpleOrderedMap<>());
        listOfMaps.add(new SimpleOrderedMap<>());
        listOfMaps.add(new SimpleOrderedMap<>());

        var result = SolrQueryResponseUtils.extractSimpleOrderedMaps(listOfMaps);

        assertThat(result).hasSize(3);
    }

    @Test
    void extractingMapsFromNonArrayObjectReturnsEmptyList() {
        assertThat(SolrQueryResponseUtils.extractSimpleOrderedMaps(new Object()))
                .hasSize(0);
    }

    @Test
    void getValuesForValidFacetField() {
        var facetFieldName = "foo";
        var facetValue = "bar";

        var solrFacetResponse = getDummySolrFacetResponse(facetFieldName, facetValue);

        assertThat(SolrQueryResponseUtils.getValuesForFacetField(solrFacetResponse, facetFieldName))
                .containsExactly(facetValue);
    }

    @Test
    void getValuesForInvalidFacetField() {
        var facetFieldName = "foo";
        var facetValue = "bar";
        var invalidFacetFieldName = "foobar";

        var solrFacetResponse = getDummySolrFacetResponse(facetFieldName, facetValue);

        assertThat(SolrQueryResponseUtils.getValuesForFacetField(solrFacetResponse, invalidFacetFieldName))
                .isEmpty();
    }

    private SimpleOrderedMap<?> getDummySolrFacetResponse(String facetFieldName, String... facetFieldValues) {
        var listOfFacets =
                Arrays.asList(facetFieldValues)
                        .stream()
                        .map(value -> {
                            var facetMap = new SimpleOrderedMap<>();
                            facetMap.add("val", value);
                            return facetMap;
                        })
                        .collect(Collectors.toList());

        var buckets = new SimpleOrderedMap<>();
        buckets.add("buckets", listOfFacets);

        var map = new SimpleOrderedMap<>();
        map.add(facetFieldName, buckets);

        return map;
    }
}
