package uk.ac.ebi.atlas.model.experiment.baseline;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
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
    public BaselineExperiment(ExperimentType experimentType,
                              String accession,
                              List<String> secondaryAccession,
                              String description,
                              Date loadDate,
                              Date lastUpdate,
                              Species species,
                              Collection<String> technologyType,
                              Collection<AssayGroup> assayGroups,
                              ExperimentDesign experimentDesign,
                              Collection<String> pubMedIds,
                              Collection<String> dois,
                              String displayName,
                              String disclaimer,
                              Collection<String> dataProviderUrls,
                              Collection<String> dataProviderDescriptions,
                              Collection<String> alternativeViews,
                              Collection<String> alternativeViewDescriptions,
                              ExperimentDisplayDefaults experimentDisplayDefaults,
                              boolean isPrivate,
                              String accessKey) {
        super(
                experimentType,
                accession,
                secondaryAccession,
                description,
                loadDate,
                lastUpdate,
                species,
                technologyType,
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
                isPrivate,
                accessKey);
    }

    @Nullable
    public FactorGroup getFactors(AssayGroup assayGroup) {
        return experimentDesign.getFactors(assayGroup.getFirstAssayId());
    }

    @Override
    protected ImmutableList<JsonObject> propertiesForAssay(String runOrAssay) {
        JsonObject result = new JsonObject();
        result.addProperty(
                "analysed",
                getDataColumnDescriptors().stream()
                        .anyMatch(assayGroup -> assayGroup.getAssayIds().contains(runOrAssay)));
        return ImmutableList.of(result);
    }
}
