package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import org.apache.solr.common.SolrInputDocument;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.SolrInputDocumentInputStream;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.profiles.IterableObjectInputStream;
import uk.ac.ebi.atlas.solr.BioentityPropertyName;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.AnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Component
public class AnalyticsIndexerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsIndexerService.class);

    private final AnalyticsCollectionProxy analyticsCollectionProxy;
    private final ExperimentDataPointStreamFactory experimentDataPointStreamFactory;

    public AnalyticsIndexerService(SolrCloudCollectionProxyFactory collectionProxyFactory,
                                   ExperimentDataPointStreamFactory experimentDataPointStreamFactory) {
        this.analyticsCollectionProxy = collectionProxyFactory.create(AnalyticsCollectionProxy.class);
        this.experimentDataPointStreamFactory = experimentDataPointStreamFactory;
    }

    public int index(@NotNull Experiment experiment,
                     @NotNull Map<@NotNull String,
                                  @NotNull Map<@NotNull BioentityPropertyName,
                                               @NotNull Set<@NotNull String>>> bioentityIdToProperties,
                     int batchSize) {

        var toLoad = new ArrayList<SolrInputDocument>(batchSize);
        var addedIntoThisBatch = 0;
        var addedInTotal = 0;

        try (var solrInputDocumentInputStream =
                new SolrInputDocumentInputStream(
                        experimentDataPointStreamFactory.stream(experiment),
                        bioentityIdToProperties)) {

            var it = new IterableObjectInputStream<>(solrInputDocumentInputStream).iterator();
            while (it.hasNext()) {
                while (addedIntoThisBatch < batchSize && it.hasNext()) {
                    var analyticsInputDocument = it.next();
                    toLoad.add(analyticsInputDocument);
                    addedIntoThisBatch++;
                }
                if (addedIntoThisBatch > 0) {
                    var updateResponse = analyticsCollectionProxy.add(toLoad);
                    LOGGER.info(
                            "Sent {} documents for {}, qTime:{}",
                            addedIntoThisBatch, experiment.getAccession(), updateResponse.getQTime());
                    addedInTotal += addedIntoThisBatch;
                    addedIntoThisBatch = 0;
                    toLoad.clear();
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        LOGGER.info("Finished: " + experiment.getAccession());
        return addedInTotal;
    }

    public void deleteExperimentFromIndex(String accession) {
        LOGGER.info("Deleting documents for {}", accession);
        var solrQueryBuilder = new SolrQueryBuilder<AnalyticsCollectionProxy>();
        solrQueryBuilder.addQueryFieldByTerm(AnalyticsCollectionProxy.EXPERIMENT_ACCESSION, accession);
        analyticsCollectionProxy.deleteByQuery(solrQueryBuilder);
        LOGGER.info("Done deleting documents for {}", accession);
    }

    public void deleteAll() {
        LOGGER.info("Deleting all documents");
        analyticsCollectionProxy.deleteAll();
        LOGGER.info("Done deleting all documents");
    }
}
