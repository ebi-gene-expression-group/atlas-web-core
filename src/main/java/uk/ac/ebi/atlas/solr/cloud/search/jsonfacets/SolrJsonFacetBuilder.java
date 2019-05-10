package uk.ac.ebi.atlas.solr.cloud.search.jsonfacets;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import java.util.Collection;

import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

public class SolrJsonFacetBuilder<T extends CollectionProxy> {
    private static final int DEFAULT_LIMIT = -1;

    private static final String DOMAIN_FILTER_TEMPLATE = "%s:%s";

    private String facetField;
    // Support "terms" facets by default
    private String facetType = SolrFacetType.TERMS.name;
    private String facetName;

    // Indicates if the builder is being used to build a nested sub-facet
    private boolean nestedFacet = false;

    private ImmutableSet.Builder<String> domainFiltersBuilder = ImmutableSet.builder();

    private ImmutableSet.Builder<SolrJsonFacetBuilder> subfacetsBuilder = ImmutableSet.builder();

    private int limit = DEFAULT_LIMIT;

    public final <U extends SchemaField<T>> SolrJsonFacetBuilder<T> setFacetField(U field) {
        facetField = field.name();
        // If the facet name hasn't been set yet, set it to the name of the field
        if (StringUtils.isBlank(facetName)) {
            facetName = field.name();
        }
        return this;
    }

    public final SolrJsonFacetBuilder<T> setFacetType(SolrFacetType type) {
        facetType = type.name;
        return this;
    }

    public final SolrJsonFacetBuilder<T> setFacetName(String name) {
        facetName = name;
        return this;
    }

    public final SolrJsonFacetBuilder<T> setNestedFacet(boolean nestedFacet) {
        this.nestedFacet = nestedFacet;
        return this;
    }

    public final SolrJsonFacetBuilder<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    // All sub-facets MUST have nestedFacet set to true
    public final SolrJsonFacetBuilder<T> addSubFacets(Collection<SolrJsonFacetBuilder<T>> subFacets) {
        subfacetsBuilder.addAll(subFacets);
        return this;
    }

    public final <U extends SchemaField<T>> SolrJsonFacetBuilder<T> addDomainFilter(U field, String value) {
        domainFiltersBuilder.add(
                String.format(
                        DOMAIN_FILTER_TEMPLATE,
                        field.name(),
                        value));
        return this;
    }

    public JsonObject build() {
        if (StringUtils.isBlank(facetType)) {
            throw new IllegalArgumentException("A facet type must be set.");
        }

        if (StringUtils.isBlank(facetField)) {
            throw new IllegalArgumentException("A facet field must be set.");
        }

        ImmutableSet<String> domainFilters = domainFiltersBuilder.build();
        ImmutableSet<SolrJsonFacetBuilder> subFacets = subfacetsBuilder.build();

        JsonObject facetWrapper = new JsonObject();

        facetWrapper.addProperty("type", facetType);
        facetWrapper.addProperty("field", facetField);
        facetWrapper.addProperty("limit", limit);

        if (!subFacets.isEmpty()) {
            JsonObject subfacetWrapper = new JsonObject();

            subFacets.forEach(facet ->
                    subfacetWrapper.add(facet.facetName, facet.build()));

            facetWrapper.add("facet", subfacetWrapper);
        }

        if (!domainFilters.isEmpty()) {
            JsonObject filters = new JsonObject();
            filters.add("filter", GSON.toJsonTree(domainFilters));

            facetWrapper.add("domain", filters);
        }

        if (nestedFacet) {
            return facetWrapper;
        }
        else {
            JsonObject result = new JsonObject();
            result.add(facetName, facetWrapper);

            return result;
        }
    }
}
