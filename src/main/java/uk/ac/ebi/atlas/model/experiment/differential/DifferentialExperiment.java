package uk.ac.ebi.atlas.model.experiment.differential;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.utils.ExperimentInfo;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class DifferentialExperiment extends Experiment<Contrast> {
    private final Set<Contrast> contrastsWithCttvPrimaryAnnotation;

    public DifferentialExperiment(@NotNull ExperimentType experimentType,
                                  @NotNull String accession,
                                  @NotNull String description,
                                  @NotNull Date lastUpdate,
                                  @NotNull Species species,
                                  @NotNull List<Pair<Contrast, Boolean>> contrasts,
                                  @NotNull ExperimentDesign experimentDesign,
                                  @NotNull Collection<String> pubMedIds,
                                  @NotNull Collection<String> dois) {
        super(
                experimentType,
                accession,
                description,
                lastUpdate,
                species,
                contrasts.stream().map(Pair::getLeft).collect(toList()),
                experimentDesign,
                pubMedIds,
                dois,
                "",
                "",
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                ExperimentDisplayDefaults.create());

        ImmutableSet<ImmutableSet<String>> uniqueAnalysedPairs = contrasts.stream()
                .map(Pair::getLeft)
                .map(contrast -> ImmutableSet.of(contrast.getReferenceAssayGroup().getId(), contrast.getTestAssayGroup().getId()))
                .collect(toImmutableSet());
        checkArgument(
                uniqueAnalysedPairs.size() == contrasts.size(),
                "Experiment cannot contain two contrasts with the same reference and test assay groups");


        this.contrastsWithCttvPrimaryAnnotation =
                contrasts.stream().filter(Pair::getRight).map(Pair::getLeft).collect(toSet());
    }

    public boolean doesContrastHaveCttvPrimaryAnnotation(@NotNull Contrast contrast) {
        return contrastsWithCttvPrimaryAnnotation.contains(contrast);
    }

    @Override
    @NotNull
    public ExperimentInfo buildExperimentInfo() {
        ExperimentInfo experimentInfo = super.buildExperimentInfo();
        experimentInfo.setNumberOfContrasts(getDataColumnDescriptors().size());
        return experimentInfo;
    }

    @Override
    @NotNull
    protected JsonObject propertiesForAssay(@NotNull String runOrAssay) {
        ImmutableList<Pair<String, String>> qualifiedContrasts =
                Stream.concat(
                        getDataColumnDescriptors().stream()
                                .filter(contrast ->
                                        contrast.getReferenceAssayGroup().getAssayIds().contains(runOrAssay))
                                .map(contrast -> Pair.of(contrast.getDisplayName(), "reference")),
                        getDataColumnDescriptors().stream()
                                .filter(contrast ->
                                        contrast.getTestAssayGroup().getAssayIds().contains(runOrAssay))
                                .map(contrast -> Pair.of(contrast.getDisplayName(), "test")))
                .collect(toImmutableList());


        JsonObject result = new JsonObject();
        result.addProperty(
                "contrastName",
                qualifiedContrasts.isEmpty() ?
                        "None" :
                        qualifiedContrasts.stream().map(Pair::getLeft).collect(joining(" / ")));
        result.addProperty(
                "referenceOrTest",
                qualifiedContrasts.isEmpty() ?
                        "" :
                        qualifiedContrasts.stream().map(Pair::getRight).collect(joining(" / ")));

        return result;
    }
}
