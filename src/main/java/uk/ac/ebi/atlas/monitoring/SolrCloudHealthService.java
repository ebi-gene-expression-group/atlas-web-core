package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toMap;

@Component
public class SolrCloudHealthService {
    private final CloudSolrClient cloudSolrClient;

    public SolrCloudHealthService(CloudSolrClient cloudSolrClient) {
        this.cloudSolrClient = cloudSolrClient;
    }

    public boolean areCollectionsUp(Collection<String> collectionNames, Collection<String> collectionAliases)
            throws IOException, SolrServerException {
        var response = cloudSolrClient.request(new CollectionAdminRequest.ClusterStatus());

        var allCollectionNames =
                ImmutableSet.<String>builder()
                        .addAll(collectionNames)
                        // Get real collection names for each alias
                        .addAll(
                                collectionAliases.stream()
                                        .map(alias ->
                                                getCollectionNameForAlias(response, alias)).collect(toImmutableSet()))
                .build();

        return allCollectionNames
                .stream()
                .map(collection -> getInactiveShardStatusesForCollection(response, collection))
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .isEmpty();
    }

    // Retrieves the collection name associated with an alias, e.g. the scxa-analytics alias returns scxa-analytics-v2
    private String getCollectionNameForAlias(NamedList<Object> response, String alias) {
        var aliases = (LinkedHashMap) response.findRecursive("cluster", "aliases");
        var collectionName = aliases.get(alias);

        if (collectionName != null) {
            return collectionName.toString();
        } else {
            throw new RuntimeException("The alias " + alias + " does not match any collections in Solr");
        }
    }

    // Returns a set of statuses that are not "active" for each node in a shard for a given Solr collection.
    private Set<String> getInactiveShardStatusesForCollection(NamedList<Object> response, String collectionName) {
        var collectionStatus = (LinkedHashMap) response.findRecursive("cluster", "collections", collectionName);

        if (MapUtils.isEmpty(collectionStatus)) {
            throw new RuntimeException("The collection " + collectionName + " does not exist in Solr");
        } else {
            collectionStatus.get("shards");
            var shards = (LinkedHashMap) collectionStatus.get("shards");

            var replicas = mapOfLinkedHashMap(shards).values().stream()
                    .map(x -> x.get("replicas"))
                    .map(LinkedHashMap.class::cast)
                    .collect(Collectors.toList());

            var inactiveStatuses = new HashSet<String>();

            replicas.forEach(replica -> {
                var replicaNodesStream = mapOfLinkedHashMap(replica).values().stream();

                replicaNodesStream.forEach(node -> {
                    var nodeStatus = node.get("state").toString();
                    if (!nodeStatus.equalsIgnoreCase("active")) {
                        inactiveStatuses.add(nodeStatus);
                    }
                });
            });

            return inactiveStatuses;
        }
    }

    private static Map<String, LinkedHashMap> mapOfLinkedHashMap(LinkedHashMap<?, ?> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof LinkedHashMap)
                .collect(toMap(
                        entry -> (String) entry.getKey(),
                        entry -> (LinkedHashMap) entry.getValue()));
    }
}
