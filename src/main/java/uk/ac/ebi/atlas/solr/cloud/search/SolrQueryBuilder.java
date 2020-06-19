package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.joining;
import static org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

public class SolrQueryBuilder<T extends CollectionProxy<?>> {
    // Some magic Solr number, from the logs:
    // ERROR (qtp511707818-76) [   ] o.a.s.s.HttpSolrCall null:java.lang.IllegalArgumentException:
    // maxSize must be <= 2147483630; got: 2147483646
    public static final int SOLR_MAX_ROWS = 2147483630;
    public static final int DEFAULT_ROWS = 100000;

    private final ImmutableSet.Builder<String> fqClausesBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<String> qClausesBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<String> flBuilder = ImmutableSet.builder();
    private final ImmutableList.Builder<SortClause> sortBuilder = ImmutableList.builder();
    private final ImmutableMap.Builder<String, SolrJsonFacetBuilder<T>> facetsBuilder = ImmutableMap.builder();

    private int rows = DEFAULT_ROWS;
    private boolean normalize = true;

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByTerm(U field, Collection<String> values) {
        fqClausesBuilder.add(SolrQueryUtils.createOrBooleanQuery(field, values, normalize));
        return this;
    }

    // Convenience method when filtering by a single value
    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByTerm(U field, String value) {
        return addFilterFieldByTerm(field, ImmutableSet.of(value));
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByRangeMin(U field, double min) {
        qClausesBuilder.add(String.format("%s:[%s TO *}", field.name(), min));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByRangeMax(U field, double max) {
        qClausesBuilder.add(String.format("%s:{* TO %s]", field.name(), max));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByOpenRangeMinMax(U field,
                                                                                         double min,
                                                                                         double max) {
        qClausesBuilder.add(String.format("%s:({* TO %s] OR [%s TO *})", field.name(), min, max));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByTerm(U field, Collection<String> values) {
        qClausesBuilder.add(SolrQueryUtils.createOrBooleanQuery(field, values, normalize));
        return this;
    }

    // Allows OR-ing together of one or multiple schema fields, i.e. (fieldA: x OR fieldA: y OR fieldB: z)
    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByTerm(Map<U, Collection<String>> fieldsAndValues) {
        String clause = fieldsAndValues
                .entrySet()
                .stream()
                .map(fieldAndValue ->
                        SolrQueryUtils.createOrBooleanQuery(
                                fieldAndValue.getKey(), fieldAndValue.getValue(), normalize))
                .collect(joining(" OR "));

        qClausesBuilder.add("(" + clause + ")");
        return this;
    }

    // Convenience method when querying a single value
    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByTerm(U field, String value) {
        return addQueryFieldByTerm(field, ImmutableSet.of(value));
    }

    public final <U extends SchemaField<T>> SolrQueryBuilder<T> setFieldList(Collection<U> fields) {
        for (SchemaField<T> field : fields) {
            flBuilder.add(field.name());
        }
        return this;
    }

    public final <U extends SchemaField<T>> SolrQueryBuilder<T> setFieldList(U field) {
        return setFieldList(ImmutableSet.of(field));
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> sortBy(U field, SolrQuery.ORDER order) {
        sortBuilder.add(new SortClause(field.name(), order));
        return this;
    }

    public SolrQueryBuilder<T> setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public SolrQueryBuilder<T> addFacet(String facetName, SolrJsonFacetBuilder<T> solrJsonFacetBuilder) {
        facetsBuilder.put(facetName, solrJsonFacetBuilder);
        return this;
    }

    public SolrQueryBuilder<T> setNormalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    public SolrQuery build() {
        ImmutableSet<String> fqClauses = fqClausesBuilder.build();
        ImmutableSet<String> qClauses = qClausesBuilder.build();
        ImmutableSet<String> fl = flBuilder.build();
        ImmutableList<SortClause> sorts = sortBuilder.build();

        var facets =
                facetsBuilder.build().entrySet().stream()
                        .collect(toImmutableMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().build()));

        return new SolrQuery()
                .addFilterQuery(fqClauses.toArray(new String[0]))
                .setQuery(
                        qClauses.isEmpty() ?
                                "*:*" :
                                qClauses.stream().filter(StringUtils::isNotBlank).collect(joining(" AND ")))
                .setFields(
                        fl.isEmpty() ?
                                "*" :
                                fl.stream().filter(StringUtils::isNotBlank).collect(joining(",")))
                .setParam("json.facet", GSON.toJson(facets))
                .setSorts(sorts)
                .setRows(rows);
    }

    private static class SolrQueryUtils {
        private SolrQueryUtils() {
            throw new UnsupportedOperationException();
        }

        // I don’t think using the Standard Query Parser
        // (https://lucene.apache.org/solr/guide/7_1/the-standard-query-parser.html#the-standard-query-parser) for
        // fields such as assay_group_id or experiment_accession incurs in a performance penalty since there’s no
        // analysis that can be done for those fields. My educated guess is that the term(s) query parser improves
        // performance when it’s used on an analyzed field because it avoids that processing step.
        private static final String FIELD_QUERY_TEMPLATE = "%s:(%s)";
        private static final String EXCLUDE_FIELD_QUERY_TEMPLATE = "!%s:*";

        private static String normalize(String str) {
            return "\"" + escapeQueryChars(str.trim()) + "\"";
        }

        private static String createOrBooleanQuery(SchemaField<?> field, Collection<String> values, boolean normalize) {
            return values.stream().anyMatch(StringUtils::isNotBlank) ?
                    String.format(
                            FIELD_QUERY_TEMPLATE,
                            field.name(),
                            values.stream()
                                    .filter(StringUtils::isNotBlank)
                                    .map(value -> normalize ? normalize(value) : value)
                                    .distinct()
                                    .collect(joining(" OR "))) :
                    String.format(EXCLUDE_FIELD_QUERY_TEMPLATE, field.name());
        }
    }
}
