package uk.ac.ebi.atlas.search.suggester;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import uk.ac.ebi.atlas.solr.analytics.AnalyticsPropertyName;
import uk.ac.ebi.atlas.utils.GsonProvider;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.atlas.solr.analytics.AnalyticsPropertyName.METADATA;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.ID_PROPERTY_NAMES;

public class SolrSuggestionReactSelectAdapter {
    // Where, and in what order, should we search in case of a free text query (without category)
    public static final ImmutableList<AnalyticsPropertyName> ANALYTICS_SUGGESTER_NAMES_FOR_METADATA_SEARCH =
            ImmutableList.of(METADATA);

    protected SolrSuggestionReactSelectAdapter() {
        throw new UnsupportedOperationException();
    }

    public static JsonArray serialize(Stream<Map<String, String>> suggestions) {
        // get("label") returns a String ; get("value") returns a Map (the entry itself)
        var groupedSuggestions =
                suggestions
                        .sorted(Comparator.comparing(suggestion -> (suggestion.get("term"))))
                        .collect(
                                groupingBy(
                                        suggestion -> suggestion.get("category"),
                                        mapping(suggestion ->
                                                        ImmutableMap.of(
                                                                "label", suggestion.get("term"),
                                                                "value", GsonProvider.GSON.toJson(suggestion)),
                                                toList())));

        JsonArray jsonArray = new JsonArray();

        ID_PROPERTY_NAMES.stream()
                .filter(propertyName -> groupedSuggestions.keySet().contains(propertyName.name))
                .forEach(propertyName ->
                    jsonArray.add(
                            GsonProvider.GSON.toJsonTree(
                                    ImmutableMap.of(
                                            "label", propertyName.label,
                                            "options", groupedSuggestions.get(propertyName.name)))));

        return jsonArray;
    }

    public static JsonArray metaDataSerialize(Stream<Map<String, String>> suggestions) {
        // get("label") returns a String ; get("value") returns a Map (the entry itself)
        var groupedSuggestions =
                suggestions
                        .sorted(Comparator.comparing(suggestion -> (suggestion.get("term"))))
                        .collect(
                                groupingBy(
                                        suggestion -> suggestion.get("category"),
                                        mapping(suggestion ->
                                                        ImmutableMap.of(
                                                                "label", suggestion.get("term"),
                                                                "value", GsonProvider.GSON.toJson(suggestion)),
                                                toList())));

        JsonArray jsonArray = new JsonArray();

        ANALYTICS_SUGGESTER_NAMES_FOR_METADATA_SEARCH.stream()
                .filter(propertyName -> groupedSuggestions.keySet().contains(propertyName.name))
                .forEach(propertyName ->
                        jsonArray.add(
                                GsonProvider.GSON.toJsonTree(
                                        ImmutableMap.of(
                                                "label", propertyName.label,
                                                "options", groupedSuggestions.get(propertyName.name)))));

        return jsonArray;
    }
}
