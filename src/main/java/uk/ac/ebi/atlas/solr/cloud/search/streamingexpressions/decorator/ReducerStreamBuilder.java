package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import org.apache.solr.client.solrj.io.comp.ComparatorOrder;
import org.apache.solr.client.solrj.io.comp.FieldComparator;
import org.apache.solr.client.solrj.io.ops.GroupOperation;
import org.apache.solr.client.solrj.io.stream.ReducerStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ReducerStreamBuilder extends TupleStreamBuilder {
    private final TupleStreamBuilder tupleStreamBuilder;
    private final String fieldName;
    private final int groupBySize;

    public ReducerStreamBuilder(TupleStreamBuilder tupleStreamBuilder, String fieldName, int groupBySize) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.fieldName = fieldName;
        this.groupBySize = groupBySize;
    }

    @Override
    protected TupleStream getRawTupleStream() {
        try {
            var fieldComparator = new FieldComparator(fieldName, ComparatorOrder.ASCENDING);
            return new ReducerStream(
                    tupleStreamBuilder.build(),
                    fieldComparator,
                    new GroupOperation(fieldComparator, groupBySize));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}