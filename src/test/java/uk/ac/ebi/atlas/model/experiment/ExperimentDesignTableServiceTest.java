package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.trader.ExperimentDesignDao;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentDesignTableServiceTest {

    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Mock
    private ExperimentDesignDao experimentDesignDaoMock;

    @Mock
    private ExperimentTrader experimentTraderMock;

    private ExperimentDesignTableService subject;

    @Test
    void hasCorrectJsonFormat() {
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
        when(experimentTraderMock.getExperiment(experiment.getAccession(), ""))
                .thenReturn(experiment);

        subject = new ExperimentDesignTableService(experimentDesignDaoMock, experimentTraderMock);

        var result = subject.getExperimentDesignData(
                experiment.getAccession(),
                pageNo,
                pageSize);

        assertThat(result.get("headers").isJsonArray()).isTrue();
        assertThat(result.get("data").isJsonArray()).isTrue();
        assertThat(result.get("totalNoOfRows").getAsInt()).isEqualTo(100);
    }

}