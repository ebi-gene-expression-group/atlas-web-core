package uk.ac.ebi.atlas.bioentity.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.controllers.BioentityNotFoundException;
import uk.ac.ebi.atlas.solr.BioentityPropertyName;
import uk.ac.ebi.atlas.solr.bioentities.query.BioentitiesSolrClient;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source.SearchStreamBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ebi.atlas.bioentity.properties.BioEntityCardProperties.BIOENTITY_PROPERTY_NAMES;
import static uk.ac.ebi.atlas.solr.BioentityPropertyName.ENSGENE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER_SEARCH;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.IS_PRIVATE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder.SOLR_MAX_ROWS;

@Component
public class BioEntityPropertyDao {
    private final BioentitiesSolrClient solrClient;
    private final BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;
    private final BioentitiesCollectionProxy bioentitiesCollectionProxy;

    public BioEntityPropertyDao(BioentitiesSolrClient gxaSolrClient,
                                SolrCloudCollectionProxyFactory collectionProxyFactory) {
        this.solrClient = gxaSolrClient;
        this.bulkAnalyticsCollectionProxy = collectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
        this.bioentitiesCollectionProxy = collectionProxyFactory.create(BioentitiesCollectionProxy.class);
    }

    public Set<String> fetchPropertyValuesForGeneId(String identifier, BioentityPropertyName propertyName) {
        return solrClient.getMap(identifier, ImmutableList.of(propertyName)).values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public Set<String> fetchGeneIdsForPropertyValue(BioentityPropertyName bioentityPropertyName,
                                                    String bioentityPropertyValue) {
        return solrClient.getBioentityIdentifiers(bioentityPropertyName, bioentityPropertyValue);
    }

    @Cacheable("bioentityProperties")
    public Map<BioentityPropertyName, Set<String>> fetchGenePageProperties(String identifier) {
        var propertiesByName = solrClient.getMap(identifier, BIOENTITY_PROPERTY_NAMES);

        if (propertiesByName.isEmpty()) {
            var solrQueryBuilder =
                    new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                            .addFilterFieldByTerm(IS_PRIVATE, "false")
                            .addQueryFieldByTerm(BIOENTITY_IDENTIFIER_SEARCH, identifier);

            if (bulkAnalyticsCollectionProxy.query(solrQueryBuilder).getResults().isEmpty()) {
                throw new BioentityNotFoundException("Gene/protein <em>" + identifier + "</em> not found.");
            } else {
                // We can do this because propertiesByName is a HashMap; arguably we should create a copy of the map if
                // we are to inject new entries
                propertiesByName.put(ENSGENE, ImmutableSet.of(identifier));
            }
        }

        return propertiesByName;
    }

    public ImmutableMap<String, String> getSymbolForGeneId(String geneId) {
        var bioentitiesQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(BIOENTITY_IDENTIFIER, geneId)
                        .addQueryFieldByTerm(PROPERTY_NAME, BioentityPropertyName.SYMBOL.name())
                        .setFieldList(Arrays.asList(BIOENTITY_IDENTIFIER, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, SolrQuery.ORDER.asc)
                        .setRows(SOLR_MAX_ROWS);

        var bioentitiesSearchBuilder = new SearchStreamBuilder<>(bioentitiesCollectionProxy, bioentitiesQueryBuilder);

        try (var tupleStreamer = TupleStreamer.of(bioentitiesSearchBuilder.build())) {
            return tupleStreamer
                    .get()
                    .collect(toImmutableMap(
                            tuple -> tuple.getString("bioentity_identifier"),
                            tuple -> tuple.getString("property_value")));
        }
    }

    public ImmutableMap<String, String> getSymbolsForGeneIds(Collection<String> geneIds) {
        var bioentitiesQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(BIOENTITY_IDENTIFIER, geneIds)
                        .addQueryFieldByTerm(PROPERTY_NAME, BioentityPropertyName.SYMBOL.name())
                        .setFieldList(Arrays.asList(BIOENTITY_IDENTIFIER, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, SolrQuery.ORDER.asc)
                        .setRows(SOLR_MAX_ROWS);

        var bioentitiesSearchBuilder = new SearchStreamBuilder<>(bioentitiesCollectionProxy, bioentitiesQueryBuilder);

        try (var tupleStreamer = TupleStreamer.of(bioentitiesSearchBuilder.build())) {
            return tupleStreamer
                    .get()
                    .collect(toImmutableMap(
                            tuple -> tuple.getString("bioentity_identifier"),
                            tuple -> tuple.getString("property_value")));
        }
    }
}
