package uk.ac.ebi.atlas.model.experiment.baseline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorGroup;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorSet;
import uk.ac.ebi.atlas.species.Species;

import java.util.Collection;
import java.util.Date;

public class BaselineExperiment extends Experiment<AssayGroup> {
    private final ImmutableMap<String, FactorSet> assayId2Factor;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineExperiment.class);

    public BaselineExperiment(ExperimentType experimentType,
                              String accession,
                              Collection<String> secondaryAccessions,
                              String description,
                              Date loadDate,
                              Date lastUpdate,
                              Species species,
                              Collection<String> technologyType,
                              Collection<AssayGroup> assayGroups,
                              ImmutableSet<String> experimentalFactorHeaders,
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
                              String accessKey,
                              ImmutableMap<String, FactorSet> assayId2Factor) {
        super(
                experimentType,
                accession,
                secondaryAccessions,
                description,
                loadDate,
                lastUpdate,
                species,
                technologyType,
                assayGroups,
                experimentalFactorHeaders,
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

        this.assayId2Factor = assayId2Factor;
    }

    @Nullable
    public FactorGroup getFactors(AssayGroup assayGroup) {
        LOGGER.info("AssayGroup: {}", assayGroup);
        LOGGER.info("AssayId2Factor: {}", assayId2Factor);
        LOGGER.info("AssayId2Factor - Factor in given assayID ({}): {}", assayGroup.getFirstAssayId(), assayId2Factor.getOrDefault(assayGroup.getFirstAssayId(), null));
        return assayId2Factor.getOrDefault(assayGroup.getFirstAssayId(), null);
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
