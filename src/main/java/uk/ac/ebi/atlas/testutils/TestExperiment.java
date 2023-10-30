package uk.ac.ebi.atlas.testutils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.BiologicalReplicate;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.species.Species;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TestExperiment extends Experiment<TestExperiment.TestSample> {
    TestExperiment(ExperimentType type,
                   String accession,
                   ImmutableSet<String> secondaryAccession,
                   String description,
                   Date loadDate,
                   Date lastUpdate,
                   Species species,
                   List<String> technologyType,
                   List<TestSample> dataColumnDescriptors,
                   ExperimentDesign experimentDesign,
                   Collection<String> pubMedIds,
                   Collection<String> dois,
                   String displayName,
                   String disclaimer,
                   List<String> dataProviderURL,
                   List<String> dataProviderDescription,
                   List<String> alternativeViews,
                   List<String> alternativeViewDescriptions,
                   ExperimentDisplayDefaults experimentDisplayDefaults,
                   boolean isPrivate,
                   String accessKey) {
        super(
                type,
                accession,
                secondaryAccession,
                description,
                loadDate,
                lastUpdate,
                species,
                technologyType,
                dataColumnDescriptors,
                experimentDesign,
                pubMedIds,
                dois,
                displayName,
                disclaimer,
                dataProviderURL,
                dataProviderDescription,
                alternativeViews,
                alternativeViewDescriptions,
                experimentDisplayDefaults,
                isPrivate,
                accessKey);
    }

    @Override
    @NotNull
    protected ImmutableList<JsonObject> propertiesForAssay(@NotNull String runOrAssay) {
        return ImmutableList.of();
    }

    // Minimal, behaviourless implementation
    public static class TestSample extends ReportsGeneExpression {
        TestSample(@NotNull String id, @NotNull Set<BiologicalReplicate> assays) {
            super(id, assays);
        }
    }
}
