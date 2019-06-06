package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ebi.atlas.experimentimport.ExperimentDao;

import java.util.Collection;

public class HealthChecker {
    private final HealthCheckService healthCheckService;
    private final ExperimentDao experimentDao;
    private final ImmutableSet<String> collectionNames;
    private final ImmutableSet<String> collectionAliases;

    public HealthChecker(HealthCheckService healthCheckService,
                         ExperimentDao experimentDao,
                         Collection<String> collectionNames,
                         Collection<String> collectionAliases) {
        this.healthCheckService = healthCheckService;
        this.experimentDao = experimentDao;
        this.collectionNames = ImmutableSet.copyOf(collectionNames);
        this.collectionAliases = ImmutableSet.copyOf(collectionAliases);
    }

    protected ImmutableMap<String, String> getHealthStatus() {
        return ImmutableMap.of(
                "solr",
                healthCheckService.isSolrUp(collectionNames, collectionAliases) ? "UP" : "DOWN",
                "db",
                healthCheckService.isDatabaseUp(experimentDao) ? "UP" : "DOWN");
    }
}
