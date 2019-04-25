package uk.ac.ebi.atlas.solr.cloud.collections;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

public class SingleCellAnalyticsCollectionProxy extends CollectionProxy {

    public static final class SingleCellAnalyticsSchemaField extends SchemaField<SingleCellAnalyticsCollectionProxy> {
        private SingleCellAnalyticsSchemaField(String fieldName) {
            super(fieldName);
        }
    }

    public static final SingleCellAnalyticsSchemaField CELL_ID =
            new SingleCellAnalyticsSchemaField("cell_id");
    public static final SingleCellAnalyticsSchemaField EXPERIMENT_ACCESSION =
            new SingleCellAnalyticsSchemaField("experiment_accession");
    public static final SingleCellAnalyticsSchemaField FACTOR_NAME =
            new SingleCellAnalyticsSchemaField("factor_name");
    public static final SingleCellAnalyticsSchemaField FACTOR_VALUE =
            new SingleCellAnalyticsSchemaField("factor_value");
    public static final SingleCellAnalyticsSchemaField CHARACTERISTIC_NAME =
            new SingleCellAnalyticsSchemaField("characteristic_name");
    public static final SingleCellAnalyticsSchemaField CHARACTERISTIC_VALUE =
            new SingleCellAnalyticsSchemaField("characteristic_value");

    public SingleCellAnalyticsCollectionProxy(SolrClient solrClient) {
        // scxa-analytics is an alias that points at scxa-analytics-vX
//        super(solrClient, "scxa-analytics");
        // TODO temporarily make proxy point to actual collection, rather than alias, to make Lime happy. Change before merging.
        super(solrClient, "scxa-analytics-v3");
    }

    public QueryResponse query(SolrQueryBuilder<SingleCellAnalyticsCollectionProxy> solrQueryBuilder) {
        return rawQuery(solrQueryBuilder.build());
    }
}
