package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.trader.ExperimentDesignDao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentDesignTableTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    @Mock
    private ExperimentDesignDao experimentDesignDaoMock;
    private ExperimentDesignTable subject;

    @Test
    void hasTotalTableRowsObject() {
        var experiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .build();
        var pageNo = RNG.nextInt(1,10);
        var pageSize = RNG.nextInt(20,50);

        when(experimentDesignDaoMock.getTableSize(experiment.getAccession()))
                .thenReturn(100);
        when(experimentDesignDaoMock.getColumnHeaders(experiment.getAccession()))
                .thenReturn(Map.of("characteristic", ImmutableList.of("ch1"),
                        "factor", ImmutableList.of("fv1")));
        when(experimentDesignDaoMock.getExperimentDesignData(experiment.getAccession(), pageNo, pageSize*2))
                .thenReturn(ImmutableList.of(
                        new LinkedHashMap<>() {{
                            put("characteristic", ImmutableList.of("ch1"));
                        }},
                        new LinkedHashMap<>() {{
                            put("factor", ImmutableList.of("fv1"));
                        }}
                    ));

        subject = new ExperimentDesignTable(experiment, experimentDesignDaoMock);

        var result = subject.asJson(
                experiment.getAccession(),
                pageNo,
                pageSize);

        assertThat(result.get("totalNoOfRows").getAsInt()).isEqualTo(100);
    }

    @Test
    void hasTableHeaderObject() {
        var experiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .build();
        var pageNo = RNG.nextInt(1,10);
        var pageSize = RNG.nextInt(20,50);

        when(experimentDesignDaoMock.getTableSize(experiment.getAccession()))
                .thenReturn(100);
        when(experimentDesignDaoMock.getColumnHeaders(experiment.getAccession()))
                .thenReturn(Map.of("", ImmutableList.of("assay1"),
                        "characteristic", ImmutableList.of("ch1"),
                        "factor", ImmutableList.of("fv1")));
        when(experimentDesignDaoMock.getExperimentDesignData(experiment.getAccession(), pageNo, pageSize*2))
                .thenReturn(ImmutableList.of(
                        new LinkedHashMap<>() {{
                            put("characteristic", ImmutableList.of("ch1"));
                        }},
                        new LinkedHashMap<>() {{
                            put("factor", ImmutableList.of("fv1"));
                        }}
                ));

        subject = new ExperimentDesignTable(experiment, experimentDesignDaoMock);

        var result = subject.asJson(
                experiment.getAccession(),
                pageNo,
                pageSize);

        assertThat(result.get("headers").isJsonArray()).isTrue();
        assertThat(result.get("headers").getAsJsonArray().size()).isEqualTo(3);
        assertThat(result.get("headers").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString())
                .isEqualTo("");
        assertThat(result.get("headers").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString())
                .isEqualTo("Sample Characteristics");
        assertThat(result.get("headers").getAsJsonArray().get(2).getAsJsonObject().get("name").getAsString())
                .isEqualTo("Experimental Variables");
    }

    @Test
    void hasDataObject() {
        var experiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .build();
        var pageNo = RNG.nextInt(1,10);
        var pageSize = RNG.nextInt(20,50);

        when(experimentDesignDaoMock.getTableSize(experiment.getAccession()))
                .thenReturn(100);
        when(experimentDesignDaoMock.getColumnHeaders(experiment.getAccession()))
                .thenReturn(Map.of("", ImmutableList.of("assay1"),
                        "characteristic", ImmutableList.of("ch1"),
                        "factor", ImmutableList.of("fv1")));
        when(experimentDesignDaoMock.getExperimentDesignData(experiment.getAccession(), pageNo, pageSize*2))
                .thenReturn(ImmutableList.of(
                        new LinkedHashMap<>() {{
                            put("characteristic", ImmutableList.of("ch1"));
                        }},
                        new LinkedHashMap<>() {{
                            put("factor", ImmutableList.of("fv1"));
                        }}
                ));

        subject = new ExperimentDesignTable(experiment, experimentDesignDaoMock);

        var result = subject.asJson(
                experiment.getAccession(),
                pageNo,
                pageSize);

        assertThat(result.get("data").isJsonArray()).isTrue();
    }
}