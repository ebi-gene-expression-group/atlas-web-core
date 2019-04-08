package uk.ac.ebi.atlas.solr.cloud;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

// The generic type is unnecessarily convoluted, but it’s Java’s fault, see note below. In this way, at least by just
// defining subclasses properly we get type-safety and no need to implement query/deleteByQuery
public abstract class CollectionProxy<T extends SolrQueryBuilder<? extends CollectionProxy>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionProxy.class);

    public final SolrClient solrClient;
    public final String nameOrAlias;

    protected CollectionProxy(SolrClient solrClient, String nameOrAlias) {
        this.solrClient = solrClient;
        this.nameOrAlias = nameOrAlias;
    }

    // This abstract class should declare the methods below, otherwise it imposes no useful contract to subclasses:
    // public abstract QueryResponse query(SolrQueryBuilder<SELF> solrQueryBuilder);
    // public abstract UpdateResponse deleteByQuery(SolrQueryBuilder<SELF> solrQueryBuilder);
    // However, Java has no nice way of referring to a class’s own type to use in generics, let alone a safe one!
    // If you feel curious about it:
    // https://stackoverflow.com/questions/7354740/is-there-a-way-to-refer-to-the-current-type-with-a-type-variable
    // And if you finally want something to make your head spin:
    // https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern
    // Some frameworks provide solutions for this: http://manifold.systems/docs.html#the-self-type

    // This is the least we can do...
    public QueryResponse query(T solrQueryBuilder) {
        return rawQuery(solrQueryBuilder.build());
    }

    public UpdateResponse deleteByQuery(T solrQueryBuilder) {
        return deleteByRawQuery(solrQueryBuilder.build());
    }

    public final QueryResponse rawQuery(SolrQuery solrQuery) {
        return logQuery(solrQuery);
    }

    protected final FieldStatsInfo fieldStats(String fieldName, SolrQuery solrQuery) {
        solrQuery.setRows(0);
        solrQuery.setGetFieldStatistics(true);
        solrQuery.setGetFieldStatistics(fieldName);
        solrQuery.addStatsFieldCalcDistinct(fieldName, true);
        return logQuery(solrQuery).getFieldStatsInfo().get(fieldName);
    }

    public final UpdateResponse add(Collection<SolrInputDocument> docs, String requestProcessor) {
        var updateRequest = new UpdateRequest();
        updateRequest.setParam("processor", requestProcessor);
        return logCommit(updateRequest.add(docs));
    }

    public final UpdateResponse deleteAll() {
        return deleteByRawQuery(new SolrQuery("*:*"));
    }

    public final UpdateResponse deleteByRawQuery(SolrQuery solrQuery) {
        return logCommit(new UpdateRequest().deleteByQuery(solrQuery.getQuery()));
    }

    private synchronized UpdateResponse logCommit(UpdateRequest updateRequest) {
        try {
            LOGGER.info("Committing {}" + updateRequest.toString());
            return updateRequest.commit(solrClient, nameOrAlias);
        } catch (IOException | SolrServerException e) {
            logException(e);
            return rollback();
        }
    }

    private synchronized UpdateResponse rollback() {
        try {
            return solrClient.rollback();
        } catch (IOException e) {
            logException(e);
            throw new UncheckedIOException(e);
        } catch (SolrServerException e) {
            logException(e);
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private QueryResponse logQuery(SolrQuery solrQuery) {
        try {
            return solrClient.query(nameOrAlias, solrQuery, SolrRequest.METHOD.POST);
        } catch (IOException e) {
            logException(e);
            throw new UncheckedIOException(e);
        } catch (SolrServerException e) {
            logException(e);
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private void logException(Exception e) {
        LOGGER.error(
                "Problem connecting to SolrCloud {} with collection {}, full stack trace follows:\n\t{}",
                solrClient.getClass().getSimpleName(),
                nameOrAlias,
                Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n\t")));
    }
}
