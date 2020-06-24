package uk.ac.ebi.atlas.experiments;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.BaselineExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.DifferentialExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.MicroarrayExperimentBuilder;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ExperimentJsonSerializerTest {
    private static final Gson GSON = new Gson();
    
    @Test
    void ExperimentJsonSerializerIsAUtilityClassAndCannotBeInstantiated() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(ExperimentJsonSerializer::new);
    }

    private void testBaseline(JsonObject result, Experiment<?> experiment) {
        assertThat(result.get("experimentAccession").getAsString())
                .isEqualTo(experiment.getAccession());
        assertThat(result.get("experimentDescription").getAsString())
                .isEqualTo(experiment.getDescription());
        assertThat(result.get("species").getAsString())
                .isEqualTo(experiment.getSpecies().getName());
        assertThat(result.get("kingdom").getAsString())
                .isEqualTo(experiment.getSpecies().getKingdom());
        assertThat(result.get("loadDate").getAsString())
                .isEqualTo(new SimpleDateFormat("dd-MM-yyyy").format(experiment.getLoadDate()));
        assertThat(result.get("lastUpdate").getAsString())
                .isEqualTo(new SimpleDateFormat("dd-MM-yyyy").format(experiment.getLastUpdate()));
        assertThat(result.get("numberOfAssays").getAsLong())
                .isEqualTo(experiment.getAnalysedAssays().size());
        assertThat(result.get("rawExperimentType").getAsString())
                .isEqualTo(experiment.getType().toString());
        assertThat(result.get("experimentalFactors").getAsJsonArray().toString())
                .isEqualTo(GSON.toJson(experiment.getExperimentDesign().getFactorHeaders()));
        assertThat(result.get("experimentProjects").getAsJsonArray().toString())
                .isEqualTo("[]");     // Unless there’s an astronomical fluke
    }

    @Test
    void canSerializeBaselineExperiments() {
        var experiment = new BaselineExperimentBuilder().build();
        var result = ExperimentJsonSerializer.serialize(experiment);
        testBaseline(result, experiment);

        assertThat(result.get("experimentType").getAsString())
                .isEqualTo("Baseline");
        assertThat(result.get("technologyType").getAsJsonArray().toString())
                .isEqualTo(GSON.toJson(experiment.getTechnologyType()));
    }

    @Test
    void canSerializeDifferentialExperiments() {
        var experiment = new DifferentialExperimentBuilder().build();
        var result = ExperimentJsonSerializer.serialize(experiment);
        testBaseline(result, experiment);

        assertThat(result.get("experimentType").getAsString())
                .isEqualTo("Differential");
        assertThat(result.get("technologyType").getAsJsonArray().toString())
                .isEqualTo(GSON.toJson(experiment.getTechnologyType()));
        assertThat(result.get("numberOfContrasts").getAsLong())
                .isEqualTo(experiment.getDataColumnDescriptors().size());
    }

    @Test
    void canSerializeMicroarrayExperiments() {
        var experiment = new MicroarrayExperimentBuilder().build();
        var result = ExperimentJsonSerializer.serialize(experiment);
        testBaseline(result, experiment);

        assertThat(result.get("experimentType").getAsString())
                .isEqualTo("Differential");
        assertThat(result.get("technologyType").getAsJsonArray().toString())
                .isEqualTo(GSON.toJson(
                        ImmutableSet.<String>builder()
                                .addAll(experiment.getTechnologyType())
                                .addAll(experiment.getArrayDesignNames())
                                .build()));
        assertThat(result.get("numberOfContrasts").getAsLong())
                .isEqualTo(experiment.getDataColumnDescriptors().size());
        assertThat(result.get("arrayDesigns").getAsJsonArray().toString())
                .isEqualTo(GSON.toJson(experiment.getArrayDesignAccessions()));
        assertThat(result.get("arrayDesignNames").getAsJsonArray().toString())
                .isEqualTo(GSON.toJson(experiment.getArrayDesignNames()));
    }
}