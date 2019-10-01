package uk.ac.ebi.atlas.model.experiment.differential.microarray;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.utils.ExperimentInfo;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

public class MicroarrayExperiment extends DifferentialExperiment {
    private final List<ArrayDesign> arrayDesigns;

    public MicroarrayExperiment(@NotNull ExperimentType experimentType,
                                @NotNull String accession,
                                @NotNull String description,
                                @NotNull Date loadDate,
                                @NotNull Date lastUpdate,
                                @NotNull Species species,
                                @NotNull List<String> technologyType,
                                @NotNull List<Pair<Contrast, Boolean>> contrasts,
                                @NotNull ExperimentDesign experimentDesign,
                                @NotNull Collection<String> pubMedIds,
                                @NotNull Collection<String> dois,
                                @NotNull List<ArrayDesign> arrayDesigns,
                                boolean isPrivate,
                                @NotNull String accessKey) {
        super(
                experimentType,
                accession,
                description,
                loadDate,
                lastUpdate,
                species,
                technologyType,
                contrasts,
                experimentDesign,
                pubMedIds,
                dois,
                isPrivate,
                accessKey);

        checkArgument(
                !arrayDesigns.isEmpty(),
                accession + ": Microarray experiment must have at least one array design");
        this.arrayDesigns = arrayDesigns;
    }

    @NotNull
    public List<String> getArrayDesignAccessions() {
        return arrayDesigns.stream()
                .map(ArrayDesign::getAccession)
                .collect(toList());
    }

    @NotNull
    public List<@NotNull String> getArrayDesignNames() {
        return arrayDesigns.stream()
                .map(ArrayDesign::getName)
                .collect(toList());
    }

    @Override
    @NotNull
    public ExperimentInfo buildExperimentInfo() {
        return super.buildExperimentInfo()
            .setArrayDesigns(getArrayDesignAccessions())
            .setArrayDesignNames(getArrayDesignNames());
    }
}
