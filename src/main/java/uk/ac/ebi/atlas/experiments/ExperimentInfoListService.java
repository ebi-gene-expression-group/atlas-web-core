package uk.ac.ebi.atlas.experiments;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.trader.ExperimentTrader;
import uk.ac.ebi.atlas.utils.ExperimentInfo;

import java.util.Comparator;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@Component
public class ExperimentInfoListService {
    private final ExperimentTrader experimentTrader;

    public ExperimentInfoListService(ExperimentTrader experimentTrader) {
        this.experimentTrader = experimentTrader;
    }

    public String getExperimentJson(String experimentAccession, String accessKey) {
        Experiment experiment = experimentTrader.getExperiment(experimentAccession, accessKey);
        return GSON.toJson(experiment.buildExperimentInfo());
    }

    public JsonObject getExperimentsJson() {
        // TODO We can remove aaData when https://www.pivotaltracker.com/story/show/165720572 is done
        var dropdownFilters = new JsonArray();
        var projectOptions = new JsonObject();
        var kingdomOptions = new JsonObject();

        projectOptions.add("label", GSON.toJsonTree("Project"));
        projectOptions.add("options", GSON.toJsonTree(getProjectOptions()));

        kingdomOptions.add("label", GSON.toJsonTree("Kingdom"));
        kingdomOptions.add("options", GSON.toJsonTree(getKingdomOptions()));

        dropdownFilters.add(projectOptions);
        dropdownFilters.add(kingdomOptions);

        JsonObject result = new JsonObject();
        result.add("dropdownFilters", dropdownFilters);
        result.add("aaData", GSON.toJsonTree(ImmutableMap.of("aaData", listPublicExperiments())).getAsJsonObject());
        return result;
    }

    public ImmutableList<ExperimentInfo> listPublicExperiments() {
        var experimentTypePrecedenceList = ImmutableList.of(
                SINGLE_CELL_RNASEQ_MRNA_BASELINE,
                RNASEQ_MRNA_BASELINE,
                PROTEOMICS_BASELINE,
                RNASEQ_MRNA_DIFFERENTIAL,
                MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL,
                MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL,
                MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL);

        // Sort by experiment type according to the above precedence list and then by display name
        return experimentTrader.getPublicExperiments().stream()
                .sorted(Comparator
                        .<Experiment>comparingInt(experiment ->
                                experimentTypePrecedenceList.indexOf(experiment.getType()))
                        .thenComparing(Experiment::getDisplayName))
                .map(Experiment::buildExperimentInfo)
                .collect(toImmutableList());
    }

    private ImmutableList<String> getKingdomOptions() {
        return listPublicExperiments().stream()
                .map(ExperimentInfo::getKingdom)
                .distinct()
                .collect(toImmutableList());
    }

    private ImmutableList<String> getProjectOptions() {
        return  listPublicExperiments().stream()
                .map(ExperimentInfo::getExperimentProjects)
                .flatMap(project -> project.stream().map(option -> option))
                .distinct()
                .collect(toImmutableList());
    }
}
