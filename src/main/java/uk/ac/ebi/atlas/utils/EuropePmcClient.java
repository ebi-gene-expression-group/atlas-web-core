package uk.ac.ebi.atlas.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.model.Publication;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

@Component
public class EuropePmcClient {
    private static final String URL = "https://www.ebi.ac.uk/europepmc/webservices/rest/search?query={0}&format=json";

    private RestTemplate restTemplate;
    private ObjectMapper mapper;

    @Inject
    public EuropePmcClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.mapper = new ObjectMapper();
    }

    public Optional<Publication> getPublicationByDoi(String doi) {
        // Enclose query in quotes as EuropePmc only searches up to the slash for DOIs not enclosed in quotes
        doi = "DOI:" + "\"" + doi + "\"";
        return parseResponseWithOneResult(doi);
    }

    public Optional<Publication> getPublicationByPubmedId(String pubmedId) {
        pubmedId = "SRC:MED AND EXT_ID:" + "\"" + pubmedId + "\"";
        return parseResponseWithOneResult(pubmedId);
    }

    private Optional<Publication> parseResponseWithOneResult(String query) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(MessageFormat.format(URL, query), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                try {
                    JsonNode responseAsJson = mapper.readTree(response.getBody());

                    if (responseAsJson.has("resultList")) {
                        JsonNode publicationResultList = responseAsJson.get("resultList").get("result");

                        if (publicationResultList.has(0)) {
                            return Optional.of(mapper.readValue(publicationResultList.get(0).toString(), Publication.class));
                        } else {
                            return Optional.empty();
                        }
                    }

                } catch (IOException e) {
                    return Optional.empty();
                }
            }
        } catch (RestClientException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }
}
