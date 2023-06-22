package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.trader.ExperimentDesignDao;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import java.util.LinkedHashMap;
import java.util.List;
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
        var expectedTotalNoOfRows = 100;

        when(experimentDesignDaoMock.getTableSize(experiment.getAccession()))
                .thenReturn(100);
        when(experimentDesignDaoMock.getColumnHeaders(experiment.getAccession()))
                .thenReturn(Map.of("characteristic", ImmutableList.of("ch1"),
                        "factor", ImmutableList.of("fv1")));
        when(experimentDesignDaoMock.getExperimentDesignData(experiment.getAccession(), pageNo, pageSize*2))
                .thenReturn(ImmutableList.of(
                        new LinkedHashMap<>() {{
                            put("characteristic", List.of("ch1"));
                        }},
                        new LinkedHashMap<>() {{
                            put("factor", List.of("fv1"));
                        }}
                ));
        when(experimentTraderMock.getExperiment(experiment.getAccession(), ""))
                .thenReturn(experiment);

        subject = new ExperimentDesignTableService(experimentDesignDaoMock, experimentTraderMock);

        var result = subject.getExperimentDesignData(
                experiment.getAccession(),
                pageNo,
                pageSize);

        var headers = result.getAsJsonArray("headers");
        var data = result.getAsJsonArray("data");
        var totalNoOfRows = result.get("totalNoOfRows").getAsInt();

        assertThat(headers).isNotNull().isNotEmpty();
        assertThat(data).isNotNull().isNotEmpty();
        assertThat(totalNoOfRows).isEqualTo(expectedTotalNoOfRows);

    }

}