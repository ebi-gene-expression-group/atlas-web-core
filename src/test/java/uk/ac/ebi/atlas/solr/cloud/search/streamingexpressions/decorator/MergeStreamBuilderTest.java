package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.io.Tuple;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.DummyTupleStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.UncheckedIOException;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MergeStreamBuilderTest {
    private static final String SORT_FIELD = "field1";

    private final ImmutableList<Tuple> streamA = ImmutableList.of(
            new Tuple(ImmutableMap.of(SORT_FIELD, "a", "field2", "x")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "b", "field2", "y")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "c", "field2", "z")));
    private final ImmutableList<Tuple> streamB = ImmutableList.of(
            new Tuple(ImmutableMap.of(SORT_FIELD, "a", "field3", "u")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "b", "field3", "v")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "c", "field3", "w")));

    private final TupleStreamBuilder tupleStreamBuilderA = DummyTupleStreamBuilder.create(streamA, SORT_FIELD, true);
    private final TupleStreamBuilder tupleStreamBuilderB = DummyTupleStreamBuilder.create(streamB, SORT_FIELD, true);

    @Test
    void canMergeMultipleStreams() {
        var subject = new MergeStreamBuilder(ImmutableSet.of(tupleStreamBuilderA, tupleStreamBuilderB), SORT_FIELD);
        try (var tupleStreamer = TupleStreamer.of(subject.build())) {
            var results = tupleStreamer.get().collect(toImmutableList());
            assertThat(results).hasSameElementsAs(ImmutableList.<Tuple>builder().addAll(streamA).addAll(streamB).build());
        }
    }

    @Test
    void mergeStreamOfASinlgeStreamReturnsOriginalTuples() {
        var subject = new MergeStreamBuilder(ImmutableSet.of(tupleStreamBuilderA), SORT_FIELD);
        try (var tupleStreamer = TupleStreamer.of(subject.build())) {
            var results = tupleStreamer.get().collect(toImmutableList());
            assertThat(results).hasSameElementsAs(streamA);
        }
    }

    @Test
    void throwsIfMergedFieldIsNotSortField() {
        var subject = new MergeStreamBuilder(ImmutableSet.of(tupleStreamBuilderA), "field2");
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> subject.build());
    }
}