package uk.ac.ebi.atlas.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDao;
import uk.ac.ebi.atlas.solr.cloud.admin.SolrCloudAdminProxy;

import java.util.Collection;

@Component
public class HealthCheckService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);
    private SolrCloudAdminProxy solrCloudAdminProxy;

    public HealthCheckService(SolrCloudAdminProxy solrCloudAdminProxy) {
        this.solrCloudAdminProxy = solrCloudAdminProxy;
    }

    public boolean isSolrUp(Collection<String> collectionNames, Collection<String> collectionAliases) {
        try {
            return solrCloudAdminProxy.areCollectionsUp(collectionNames, collectionAliases);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public boolean isDatabaseUp(ExperimentDao experimentDao) {
        try {
            return experimentDao.countExperiments() > 0;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }
}
