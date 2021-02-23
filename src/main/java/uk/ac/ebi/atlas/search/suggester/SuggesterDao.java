package uk.ac.ebi.atlas.search.suggester;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.Suggestion;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.solr.bioentities.query.BioentitiesSolrClient;
import uk.ac.ebi.atlas.species.Species;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Component
public class SuggesterDao {
    private final BioentitiesSolrClient solrClient;

    public SuggesterDao(BioentitiesSolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public Stream<Suggestion> fetchBioentityProperties(String query,
                                                       int limit,
                                                       boolean highlight,
                                                       Species... species) {
		var bioentityPropertySuggestions =
				highlight ?
				fetchSuggestions("propertySuggester", query, limit, species) :
				fetchSuggestions("propertySuggesterNoHighlight", query, limit, species);

        return Stream.concat(bioentityPropertySuggestions, fetchAnalyticsDummySuggestions());
    }

    public Stream<Suggestion> fetchBioentityIdentifiers(String query,
                                                        int limit,
                                                        Species... species) {
        return fetchSuggestions("bioentitySuggester", query, limit, species);
    }

    private Stream<Suggestion> fetchSuggestions(String suggesterDictionary,
                                                String query,
                                                int limit,
                                                Species... species) {
        // We want the user to go beyond one keystroke to get some suggestions
        if (query.length() < 2) {
            return Stream.empty();
        }

        Comparator<Suggestion> compareByWeightLengthAlphabetical =
                Comparator
                        .comparingLong(Suggestion::getWeight).reversed()
                        .thenComparingInt(suggestion -> suggestion.getTerm().length())
                        .thenComparing(Suggestion::getTerm);

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler("/suggest")
                .setParam("suggest.dictionary", suggesterDictionary)
                .setParam("suggest.q", query)
                // We raise suggest.count to a high enough value to get exact matches (the default is 100)
                .setParam("suggest.count", "750")
                .setParam(
                        "suggest.cfq",
                        Arrays.stream(species)
                                .map(Species::getEnsemblName)
                                .collect(joining(" ")));

        return solrClient.query(solrQuery).getSuggesterResponse().getSuggestions().values().stream()
                .flatMap(List::stream)
                // The current implementation considers symbols Aspm and ASPM two different suggestions. I dont’t know
                // if that’s good or bad because I don’t know if to a biologist it’s meaningful (I guess not). If we
                // change it it should be reflected in a story.
                .distinct()
                .sorted(compareByWeightLengthAlphabetical)
                .limit(limit);
    }

	/**
	 * dummy suggesters for analytics collections
	 * @return Stream<Suggestion>
	 */
	private Stream<Suggestion> fetchAnalyticsDummySuggestions() {

		 return Stream.of(
				//disease
				new Suggestion("diabetes mellitus", 0, "endocrine pancreas disease"),
				new Suggestion("type II diabetes mellitus", 0, "type II diabetes mellitus"),
				new Suggestion("Diabetes Mellitus, Ketosis Resistant", 0, "T2DM - Type 2 Diabetes mellitus"),
				new Suggestion("T2DM - Type 2 Diabetes mellitus", 0, "T2DM - Type 2 Diabetes mellitus"),
				new Suggestion("Adult-Onset Diabetes Mellitus", 0, "T2DM - Type 2 Diabetes mellitus"),
				new Suggestion("Diabetes Mellitus, Adult-Onsets", 0, "T2DM - Type 2 Diabetes mellitus"),
				new Suggestion("Diabetes, Type 2", 0, "T2DM - Type 2 Diabetes mellitus"),
				//organism part
				new Suggestion("zone of skin", 0, "zone of skin"),
				new Suggestion("portion of skin", 0, "portion of skin"),
				new Suggestion("skin zone", 0, "portion of skin"),
				new Suggestion("region of skin", 0, "portion of skin"),
				new Suggestion("skin region", 0, "portion of skin"),
				new Suggestion("skin", 0, "portion of skin"),
				new Suggestion("lymph node", 0, "lymph node"),
				new Suggestion("mediastinal <b>lymph</b> <b>node</b>", 0, "mesenteric lymph node"),
				new Suggestion("human", 0, "man"),
				new Suggestion("female", 0, "female"),
				new Suggestion("islet of Langerhans", 0, "islet of Langerhans"),
				new Suggestion("collecting specimen from organ postmortem", 0, "collecting specimen from organ postmortem"),
				//disease
				new Suggestion("<b>tumor</b> disease", 0, "neoplastic growth"),
				new Suggestion("desmoplastic small round cell <b>tumor</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("Endolymphatic Sac <b>Tumor</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("Bladder Inflammatory Myofibroblastic <b>Tumor</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("Ovarian Microcystic Stromal <b>Tumor</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("Mixed <b>Tumor</b> of the Skin", 0, "desmoplastic small round cell tumor"),
				new Suggestion("germ cell <b>tumor</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("Calcifying Nested Epithelial Stromal <b>Tumor</b> of the Liver", 0, "desmoplastic small round cell tumor"),
				new Suggestion("mandibular <b>cancer</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("<b>cancer</b>", 0, "desmoplastic small round cell tumor"),
				new Suggestion("Calcifying Nested Epithelial Stromal <b>Tumor</b> of the Liver", 0, "desmoplastic small round cell tumor"),
				//cell type
				new Suggestion("blood vessel endothelial cell", 0, "HUVEC cell"),
				new Suggestion("B cell", 0, "B cell"),
				new Suggestion("B Cells", 0, "B-Cells")
		);
	}
}
