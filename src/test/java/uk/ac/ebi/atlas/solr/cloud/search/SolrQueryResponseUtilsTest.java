package uk.ac.ebi.atlas.solr.cloud.search;

import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolrQueryResponseUtilsTest {

    @Test
    void extractingMapsFromArrayListReturnsListOfMaps() {
        List<Object> listOfMaps = new ArrayList<>();

        listOfMaps.add(new SimpleOrderedMap());
        listOfMaps.add(new SimpleOrderedMap());
        listOfMaps.add(new SimpleOrderedMap());

        List<SimpleOrderedMap> result = SolrQueryResponseUtils.extractSimpleOrderedMaps(listOfMaps);

        assertThat(result).hasSize(3);
    }

    @Test
    void extractingMapsFromNonArrayObjectReturnsEmptyList() {
        assertThat(SolrQueryResponseUtils.extractSimpleOrderedMaps(new Object()))
                .hasSize(0);
    }
}
