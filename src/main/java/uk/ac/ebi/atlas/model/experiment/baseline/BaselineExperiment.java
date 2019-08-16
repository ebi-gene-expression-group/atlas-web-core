package uk.ac.ebi.atlas.model.experiment.baseline;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorGroup;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.species.Species;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class BaselineExperiment extends Experiment<AssayGroup> {
    private final String secondaryAccession;

    public BaselineExperiment(@NotNull ExperimentType experimentType,
                              @NotNull String accession,
                              @NotNull String secondaryAccession,
                              @NotNull String description,
                              @NotNull Date loadDate,
                              @NotNull Date lastUpdate,
                              @NotNull Species species,
                              @NotNull Collection<@NotNull AssayGroup> assayGroups,
                              @NotNull ExperimentDesign experimentDesign,
                              @NotNull Collection<@NotNull String> pubMedIds,
                              @NotNull Collection<@NotNull String> dois,
                              @NotNull String displayName,
                              @NotNull String disclaimer,
                              @NotNull List<@NotNull String> dataProviderUrls,
                              @NotNull List<@NotNull String> dataProviderDescriptions,
                              @NotNull List<@NotNull String> alternativeViews,
                              @NotNull List<@NotNull String> alternativeViewDescriptions,
                              @NotNull ExperimentDisplayDefaults experimentDisplayDefaults,
                              boolean isPrivate) {
        super(
                experimentType,
                accession,
                description,
                loadDate,
                lastUpdate,
                species,
                assayGroups,
                experimentDesign,
                pubMedIds,
                dois,
                displayName,
                disclaimer,
                dataProviderUrls,
                dataProviderDescriptions,
                alternativeViews,
                alternativeViewDescriptions,
                experimentDisplayDefaults,
                isPrivate);
        this.secondaryAccession = secondaryAccession;
    }

    @NotNull
    public String getSecondaryAccession() {
        return secondaryAccession;
    }

    @Nullable
    public FactorGroup getFactors(AssayGroup assayGroup) {
        return experimentDesign.getFactors(assayGroup.getFirstAssayId());
    }

    @Override
    @NotNull
    protected ImmutableList<JsonObject> propertiesForAssay(@NotNull String runOrAssay) {
        JsonObject result = new JsonObject();
        result.addProperty(
                "analysed",
                getDataColumnDescriptors().stream()
                        .anyMatch(assayGroup -> assayGroup.getAssayIds().contains(runOrAssay)));
        return ImmutableList.of(result);
    }
}
