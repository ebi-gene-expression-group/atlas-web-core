package uk.ac.ebi.atlas.model.experiment.singlecell;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.Cell;
import uk.ac.ebi.atlas.species.Species;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class SingleCellBaselineExperiment extends Experiment<Cell> {
    public SingleCellBaselineExperiment(@Nullable Optional<List<String>> technologyType,
                                        @NotNull ExperimentType experimentType,
                                        @NotNull String accession,
                                        @NotNull String description,
                                        @NotNull Date loadDate,
                                        @NotNull Date lastUpdate,
                                        @NotNull Species species,
                                        @NotNull List<Cell> cells,
                                        @NotNull ExperimentDesign experimentDesign,
                                        @NotNull Collection<String> pubMedIds,
                                        @NotNull Collection<String> dois,
                                        @NotNull String displayName,
                                        boolean isPrivate,
                                        @NotNull String accessKey) {
        super(
                technologyType,
                experimentType,
                accession,
                description,
                loadDate,
                lastUpdate,
                species,
                cells,
                experimentDesign,
                pubMedIds,
                dois,
                displayName,
                "",
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                ExperimentDisplayDefaults.create(),
                isPrivate,
                accessKey);
    }

    @Override
    @NotNull
    protected ImmutableList<JsonObject> propertiesForAssay(@NotNull String runOrAssay) {
        // Currently we’re ignoring on the front-end the analysed property in single cell experiments, but it must be
        // present for the logic in ExperimentDesignTable to be consistent and display a properly populated table
        JsonObject result = new JsonObject();
        result.addProperty(
                "analysed",
                getDataColumnDescriptors().stream()
                        .anyMatch(assayGroup -> assayGroup.getAssayIds().contains(runOrAssay)));
        return ImmutableList.of(result);
    }
}
