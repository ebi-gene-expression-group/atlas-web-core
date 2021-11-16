package uk.ac.ebi.atlas.search.suggester;

import java.util.Map;
import java.util.stream.Stream;

public interface AnalyticsSuggesterService {
    Stream<Map<String, String>> fetchMetaDataSuggestions(String query, String... species);
}
