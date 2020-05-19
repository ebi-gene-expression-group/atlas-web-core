package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.io.comp.ComparatorOrder;
import org.apache.solr.client.solrj.io.comp.FieldComparator;
import org.apache.solr.client.solrj.io.stream.MergeStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MergeStreamBuilder extends TupleStreamBuilder {
    private final ImmutableSet.Builder<TupleStreamBuilder> tupleStreamBuilders = ImmutableSet.builder();
    private final String fieldName;
    private final boolean descending;

    // We don’t use SchemaField<T> because field names may have been renamed by a previous select clause to any name
    public MergeStreamBuilder(ImmutableCollection<? extends TupleStreamBuilder> tupleStreamBuilders, String fieldName) {
        this.tupleStreamBuilders.addAll(tupleStreamBuilders);
        this.fieldName = fieldName;
        this.descending = false;
    }

    @Override
    protected TupleStream getRawTupleStream() {
        try {
            return new MergeStream(
                    new FieldComparator(fieldName, descending ? ComparatorOrder.DESCENDING : ComparatorOrder.ASCENDING),
                    tupleStreamBuilders.build().stream()
                            .map(TupleStreamBuilder::build)
                            .toArray(TupleStream[]::new));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
