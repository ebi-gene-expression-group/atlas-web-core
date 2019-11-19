package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.CHARACTERISTIC_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.CHARACTERISTIC_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;

@Component
public class ExperimentTraderDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SingleCellAnalyticsCollectionProxy singleCellAnalyticsCollectionProxy;

    public ExperimentTraderDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                               SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.singleCellAnalyticsCollectionProxy = solrCloudCollectionProxyFactory.create(SingleCellAnalyticsCollectionProxy.class);
    }

    public ImmutableSet<String> fetchPublicExperimentAccessions(ExperimentType... experimentTypes) {
        var experimentTypeNames =
                Stream.of(experimentTypes.length == 0 ? ExperimentType.values() : experimentTypes)
                        .map(ExperimentType::name)
                        .collect(toImmutableList());

        return ImmutableSet.copyOf(
                namedParameterJdbcTemplate.queryForList(
                        "SELECT accession FROM experiment WHERE private=FALSE AND type IN(:experimentTypes)",
                        ImmutableMap.of("experimentTypes", experimentTypeNames),
                        String.class));
    }

    public ImmutableList<String> fetchExperimentsByCharacteristicType(String characteristicName, String characteristicValue) {
        var queryBuilder =
                new SolrQueryBuilder<SingleCellAnalyticsCollectionProxy>()
                        .addQueryFieldByTerm(CHARACTERISTIC_NAME, characteristicName)
                        .addQueryFieldByTerm(CHARACTERISTIC_VALUE, characteristicValue)
                        .setFieldList(EXPERIMENT_ACCESSION);

        var results = this.singleCellAnalyticsCollectionProxy.query(queryBuilder).getResults();
        return results
                .stream()
                .map(solrDocument -> (String) solrDocument.getFieldValue(EXPERIMENT_ACCESSION.name()))
                .distinct()
                .collect(toImmutableList());
    }
}
