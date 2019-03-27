package uk.ac.ebi.atlas.experimentimport;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.ac.ebi.atlas.experimentimport.condensedSdrf.CondensedSdrfParserOutput;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

// The title is retrieved from the IDF file, see IdfParser
public class ExperimentDto {
    private final String experimentAccession;
    private final ExperimentType experimentType;
    private final String species;
    private final Set<String> pubmedIds;
    private final Set<String> dois;
    private final Date lastUpdate;
    private final boolean isPrivate;
    private final String accessKey;

    public ExperimentDto(String experimentAccession,
                         ExperimentType experimentType,
                         String species,
                         Set<String> pubmedIds,
                         Set<String> dois,
                         Date lastUpdate,
                         boolean isPrivate,
                         String accessKey) {
        this.experimentAccession = experimentAccession;
        this.experimentType = experimentType;
        this.species = species;
        this.pubmedIds = pubmedIds;
        this.dois = dois;
        this.lastUpdate = lastUpdate;
        this.isPrivate = isPrivate;
        this.accessKey = accessKey;
    }

    static ExperimentDto create(String experimentAccession,
                                ExperimentType experimentType,
                                String species,
                                Set<String> pubmedIds,
                                Set<String> dois,
                                boolean isPrivate) {
        return new ExperimentDto(
                experimentAccession,
                experimentType,
                species,
                pubmedIds,
                dois,
                null,
                isPrivate,
                UUID.randomUUID().toString());
    }

    static ExperimentDto create(CondensedSdrfParserOutput condensedSdrfParserOutput,
                                IdfParserOutput idfParserOutput,
                                String species,
                                boolean isPrivate) {
        return ExperimentDto.create(
                condensedSdrfParserOutput.getExperimentAccession(),
                condensedSdrfParserOutput.getExperimentType(),
                species,
                idfParserOutput.getPubmedIds(),
                idfParserOutput.getDois(),
                isPrivate);
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }
    public ExperimentType getExperimentType() {
        return experimentType;
    }
    public boolean isPrivate() {
        return isPrivate;
    }
    public Date getLastUpdate() {
        return lastUpdate;
    }
    public String getAccessKey() {
        return accessKey;
    }
    public String getSpecies() {
        return species;
    }
    public Set<String> getPubmedIds() {
        return pubmedIds;
    }
    public Set<String> getDois() {
        return dois;
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.add("accession", new JsonPrimitive(experimentAccession));
        result.add("type", new JsonPrimitive(experimentType.name()));
        result.add("species", new JsonPrimitive(species));
        JsonArray pubmedIdsArray = new JsonArray();
        pubmedIds.forEach(id -> pubmedIdsArray.add(new JsonPrimitive(id)));
        JsonArray doisArray = new JsonArray();
        dois.forEach(doi -> doisArray.add(new JsonPrimitive(doi)));
        result.add("pubmedIds", pubmedIdsArray);
        result.add("dois", doisArray);
        result.add("isPrivate", new JsonPrimitive(isPrivate));
        result.add("accessKey", new JsonPrimitive(accessKey));
        result.add("lastUpdate", new JsonPrimitive(lastUpdate.toString()));

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(experimentAccession, experimentType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExperimentDto) {
            ExperimentDto other = (ExperimentDto) obj;
            return this.experimentAccession.equals(other.experimentAccession) &&
                    this.experimentType.equals(other.experimentType);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ExperimentAccession", experimentAccession)
                .add("ExperimentType", experimentType)
                .add("species", species)
                .add("pubmedIds", pubmedIds)
                .add("dois", dois)
                .add("isPrivate", isPrivate)
                .add("accessKey", accessKey)
                .add("lastUpdate", lastUpdate).toString();
    }
}
