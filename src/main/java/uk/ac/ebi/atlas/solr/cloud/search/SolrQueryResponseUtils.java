package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SolrQueryResponseUtils {

    protected SolrQueryResponseUtils() {
        throw new UnsupportedOperationException();
    }

    public static List<String> getValuesForFacetField(SimpleOrderedMap map, String facetField) {
        List<SimpleOrderedMap> results = extractSimpleOrderedMaps(map.findRecursive(facetField, "buckets"));

        return results
                .stream()
                .map(x -> x.get("val").toString())
                .collect(Collectors.toList());
    }

    public static List<SimpleOrderedMap> extractSimpleOrderedMaps(Object o) {
        ImmutableList.Builder<SimpleOrderedMap> builder = ImmutableList.builder();
        if (o instanceof ArrayList) {
            for (Object element : (ArrayList) o) {
                if (element instanceof SimpleOrderedMap) {
                    builder.add((SimpleOrderedMap) element);
                }
            }
        }
        return builder.build();
    }
}
