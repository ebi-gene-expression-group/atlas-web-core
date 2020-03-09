package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.io.stream.SelectStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public class SelectStreamBuilder extends TupleStreamBuilder {
    private final TupleStreamBuilder tupleStreamBuilder;
    private final List<String> selectedFields;
    private final ImmutableMap.Builder<String, String> fieldMapsBuilder = ImmutableMap.builder();

    public SelectStreamBuilder(TupleStreamBuilder tupleStreamBuilder) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.selectedFields = List.of();
    }

    public SelectStreamBuilder(TupleStreamBuilder tupleStreamBuilder, List<String> selectedFields) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.selectedFields = selectedFields;
    }

    // public SelectStreamBuilder<T> addFieldMap(String originalfieldName, String newFieldName) {
    //     fieldMapsBuilder.put(originalfieldName, newFieldName);
    //     return this;
    // }

    public SelectStreamBuilder addFieldMapping(Map<String, String> fieldNamesMap) {
        fieldMapsBuilder.putAll(fieldNamesMap);
        return this;
    }

    @Override
    protected TupleStream getRawTupleStream() {
        try {
            return selectedFields.isEmpty() ?
                    new SelectStream(tupleStreamBuilder.build(), fieldMapsBuilder.build()) :
                    new SelectStream(tupleStreamBuilder.build(), selectedFields);
        } catch (IOException e) {
            // The truth is, this constructor can’t throw an IOException, but the class declaration does... lame
            throw new UncheckedIOException(e);
        }
    }
}
