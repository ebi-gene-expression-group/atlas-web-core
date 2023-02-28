package uk.ac.ebi.atlas.model.experiment;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import uk.ac.ebi.atlas.trader.ExperimentDesignDao;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

@Service
public class ExperimentDesignTableService {
    private final ExperimentDesignDao experimentDesignDao;

    private final ExperimentTrader experimentTrader;

    public ExperimentDesignTableService(ExperimentDesignDao experimentDesignDao,
                                             ExperimentTrader experimentTrader) {
        this.experimentDesignDao = experimentDesignDao;
        this.experimentTrader = experimentTrader;
    }

    public JsonObject getExperimentDesignData(String experiment_accession, int pageNo, int pageSize) {
        var experimentDesignTable = createExperimentDesignTable(experiment_accession, pageNo, pageSize);
        return experimentDesignTable.asJson(experiment_accession, pageNo, pageSize);
    }

    private ExperimentDesignTable createExperimentDesignTable(String experiment_accession, int pageNo, int pageSize) {
        var experiment = experimentTrader.getExperiment(experiment_accession, "");
        return new ExperimentDesignTable(experiment, experimentDesignDao);
    }
}
