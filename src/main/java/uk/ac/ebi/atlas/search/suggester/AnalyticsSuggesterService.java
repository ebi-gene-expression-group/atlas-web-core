package uk.ac.ebi.atlas.search.suggester;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Stream;


public interface AnalyticsSuggesterService {
    Stream<Map<String, String>> fetchOntologyAnnotationSuggestions(String query, String... species);
}
