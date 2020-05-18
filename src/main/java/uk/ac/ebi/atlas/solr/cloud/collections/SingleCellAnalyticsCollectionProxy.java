package uk.ac.ebi.atlas.solr.cloud.collections;

import org.apache.solr.client.solrj.SolrClient;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

public class SingleCellAnalyticsCollectionProxy extends CollectionProxy<SingleCellAnalyticsCollectionProxy> {

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
    public static final SingleCellAnalyticsSchemaField FACET_FACTOR_VALUE =
            new SingleCellAnalyticsSchemaField("facet_factor_value");
    public static final SingleCellAnalyticsSchemaField CHARACTERISTIC_NAME =
            new SingleCellAnalyticsSchemaField("characteristic_name");
    public static final SingleCellAnalyticsSchemaField CHARACTERISTIC_VALUE =
            new SingleCellAnalyticsSchemaField("characteristic_value");
    public static final SingleCellAnalyticsSchemaField FACET_CHARACTERISTIC_VALUE =
            new SingleCellAnalyticsSchemaField("facet_characteristic_value");
    public static final SingleCellAnalyticsSchemaField ONTOLOGY_ANNOTATION_ANCESTORS_URIS =
            new SingleCellAnalyticsSchemaField("ontology_annotation_ancestors_uris_s");
    public static final SingleCellAnalyticsSchemaField ONTOLOGY_ANNOTATION =
            new SingleCellAnalyticsSchemaField("ontology_annotation");
    public static final SingleCellAnalyticsSchemaField ONTOLOGY_ANNOTATION_LABEL =
            new SingleCellAnalyticsSchemaField("ontology_annotation_label_t");
    public static final SingleCellAnalyticsSchemaField ONTOLOGY_ANNOTATION_ANCESTORS_LABELS =
            new SingleCellAnalyticsSchemaField("ontology_annotation_ancestors_labels_t");

    public SingleCellAnalyticsCollectionProxy(SolrClient solrClient) {
        // scxa-analytics is an alias that points at scxa-analytics-vX
//        super(solrClient, "scxa-analytics");
        // TODO temporarily make proxy point to actual collection, rather than alias, to make Lime happy. Change before merging.
        super(solrClient, "scxa-analytics-v3");
    }
}
