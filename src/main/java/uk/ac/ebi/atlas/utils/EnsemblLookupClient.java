package uk.ac.ebi.atlas.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@Named
@Profile("!cli")
public class EnsemblLookupClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnsemblLookupClient.class);
    private static final String SPECIES_FIELD = "species";

    private final RestTemplate restTemplate;
    private final SpeciesFactory speciesFactory;
    private final String baseUrl;

    protected EnsemblLookupClient() {
        this.restTemplate = null;
        this.speciesFactory = null;
        this.baseUrl = "";
    }

    @Inject
    public EnsemblLookupClient(RestTemplate restTemplate,
                               SpeciesFactory speciesFactory,
                               @Value("${ensembl.rest.base.url:https://rest.ensembl.org}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.speciesFactory = speciesFactory;
        this.baseUrl = baseUrl;
    }

    public static boolean isEnsemblId(String identifier) {
        return identifier != null && identifier.startsWith("ENS");
    }

    @Cacheable(
            cacheNames = "ensemblSpecies",
            key = "#ensemblId",
            unless = "#result == null")
    public Optional<Species> lookupSpecies(String ensemblId) {
        if (!isEnsemblId(ensemblId)) {
            return Optional.empty();
        }

        try {
            var headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
            var request = new HttpEntity<>(headers);

            var url = baseUrl + "/lookup/id/" + ensemblId + "?content-type=application/json";
            var response = restTemplate.exchange(url, HttpMethod.GET, request, String.class).getBody();

            return parseSpeciesResponse(ensemblId, response);
        } catch (RestClientException e) {
            LOGGER.error("There was an error looking up species for Ensembl ID {}", ensemblId);
            return Optional.empty();
        }
    }

    private Optional<Species> parseSpeciesResponse(String ensemblId, String response) {
        if (response == null || response.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonObject jsonObject = GSON.fromJson(response, JsonObject.class);
            if (jsonObject == null || !jsonObject.has(SPECIES_FIELD) || !jsonObject.get(SPECIES_FIELD).isJsonPrimitive()) {
                LOGGER.warn("Species for Ensembl ID {} could not be found", ensemblId);
                return Optional.empty();
            }

            return Optional.of(speciesFactory.create(jsonObject.get(SPECIES_FIELD).getAsString()));
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON returned from Ensembl API for ID {}", ensemblId);
            return Optional.empty();
        }
    }
}
