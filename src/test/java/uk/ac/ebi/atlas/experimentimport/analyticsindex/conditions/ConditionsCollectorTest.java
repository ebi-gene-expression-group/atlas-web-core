package uk.ac.ebi.atlas.experimentimport.analyticsindex.conditions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.testutils.AssayGroupFactory;
import uk.ac.ebi.atlas.trader.ExperimentDesignParser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomEfoAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomRnaSeqRunId;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConditionsCollectorTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Mock
    private ExperimentDesign experimentDesignMock;

    @Mock
    private BaselineExperiment baselineExperimentMock;

    @Mock
    private DifferentialExperiment differentialExperimentMock;

    @Mock
    private EfoLookupService efoLookupServiceMock;

    @Mock
    private ExperimentDesignParser experimentDesignParserMock;

    private ImmutableSetMultimap<String, String> expandedOntologyTerms;

    private ImmutableSet<String> expandedOntologyTermLabels;

    private Map<String, Map<String, String>> factorValues;

    private Map<String, Map<String, String>> sampleCharacteristics;

    private AssayGroup testAssayGroup;

    private AssayGroup referenceAssayGroup;

    private ConditionsCollector subject;

    @BeforeEach
    void setUp() {
        String experimentAccession = generateRandomExperimentAccession();
        when(differentialExperimentMock.getAccession()).thenReturn(experimentAccession);
        when(baselineExperimentMock.getAccession()).thenReturn(experimentAccession);

        subject = new ConditionsCollector(efoLookupServiceMock, experimentDesignParserMock);

        when(experimentDesignParserMock.parse(baselineExperimentMock.getAccession()))
                .thenReturn(experimentDesignMock);
        when(experimentDesignParserMock.parse(differentialExperimentMock.getAccession()))
                .thenReturn(experimentDesignMock);

        // Create a pool of 50 random assays...
        var assayIds =
                IntStream.range(0, 50)
                        .boxed()
                        .map(__ -> generateRandomRnaSeqRunId())
                        .distinct()
                        .collect(toList());

        // Assign at most five random EFO terms to each assay
        var assayIdToOntologyTerms = assayIds.stream()
                .collect(flatteningToImmutableSetMultimap(
                        assayId -> assayId,
                        __ -> IntStream.range(0, RNG.nextInt(1, 5))
                                .boxed()
                                .map(___ -> generateRandomEfoAccession())
                                .distinct()));
        when(experimentDesignMock.getAllOntologyTermIdsByAssayAccession()).thenReturn(assayIdToOntologyTerms);

        // Expand, in turn, each EFO term to at most another five
        expandedOntologyTerms =
                assayIdToOntologyTerms.entries().stream()
                        .collect(flatteningToImmutableSetMultimap(
                                Map.Entry::getKey,
                                entry ->
                                        Stream.concat(
                                                Stream.of(entry.getValue()),
                                                IntStream.range(0, RNG.nextInt(1, 5))
                                                        .boxed()
                                                        .map(__ -> generateRandomEfoAccession()))
                                        .distinct()));
        when(efoLookupServiceMock.expandOntologyTerms(assayIdToOntologyTerms)).thenReturn(expandedOntologyTerms);

        // Assign names to all EFO terms
        expandedOntologyTermLabels =
                expandedOntologyTerms.values().stream().map(__ -> randomAlphabetic(15)).collect(toImmutableSet());
        // Returning the full set is a bit crude, but Mockito doesn’t have e.g. lambdas for returns
        when(efoLookupServiceMock.getLabels(argThat(efoTerms -> expandedOntologyTerms.values().containsAll(efoTerms))))
                .thenReturn(expandedOntologyTermLabels);

        // Factors and sample characteristics from SDRF file. assay accession -> map of header, value (max of 4)
        var factorHeaders =
                IntStream.range(0, RNG.nextInt(1, 4))
                        .boxed()
                        .map(__ -> randomAlphabetic(10))
                        .collect(toSet());
        factorValues =
                assayIds.stream()
                        .collect(toMap(
                                assayId -> assayId,
                                __ -> factorHeaders.stream()
                                        .collect(toMap(
                                                factorHeader -> factorHeader,
                                                ___ -> randomAlphanumeric(20)))));
        factorValues.forEach(
                (key, value) -> when(experimentDesignMock.getFactorValues(key)).thenReturn(value));

        var sampleHeaders =
                IntStream.range(0, RNG.nextInt(1, 4))
                        .boxed()
                        .map(__ -> randomAlphabetic(10))
                        .collect(toSet());
        sampleCharacteristics =
                assayIds.stream()
                        .collect(toMap(
                                assayId -> assayId,
                                __ -> sampleHeaders.stream()
                                        .collect(toMap(
                                                sampleHeader -> sampleHeader,
                                                ___ -> randomAlphanumeric(20)))));
        sampleCharacteristics.forEach(
                (key, value) -> when(experimentDesignMock.getSampleCharacteristicsValues(key)).thenReturn(value));

        // Create list of 1..10 assay groups, named g1, g2, g3...
        // We need at least two to have a reference and a test below
        var assayGroupIds =
                IntStream.range(1, RNG.nextInt(3, 11))
                        .boxed()
                        .map(i -> "g" + i)
                        .collect(toList());

        // Distribute assays between assay groups
        var assayGroupToAssayIds = randomizedMultimapOf(assayGroupIds, assayIds);

        var assayGroups = assayGroupToAssayIds.keySet().stream()
                .map(assayGroupId ->
                        AssayGroupFactory.create(
                                assayGroupId,
                                assayGroupToAssayIds.get(assayGroupId).toArray(new String[0])))
                .collect(toImmutableList());

        var testReferenceAssayGroups = Pair.of(RNG.nextInt(0, assayGroups.size()), RNG.nextInt(0, assayGroups.size()));
        while (testReferenceAssayGroups.getLeft().equals(testReferenceAssayGroups.getRight())) {
            // Reroll!
            testReferenceAssayGroups = Pair.of(RNG.nextInt(0, assayGroups.size()), RNG.nextInt(0, assayGroups.size()));
        }

        testAssayGroup = assayGroups.get(testReferenceAssayGroups.getLeft());
        referenceAssayGroup = assayGroups.get(testReferenceAssayGroups.getRight());
        var contrast = new Contrast(
                testAssayGroup.getId() + "_" + referenceAssayGroup.getId(),
                "‘" + testAssayGroup.getId() + "’ vs ‘" + referenceAssayGroup.getId() + "’",
                referenceAssayGroup,
                testAssayGroup,
                randomAlphanumeric(10));

        when(baselineExperimentMock.getDataColumnDescriptors()).thenReturn(assayGroups);
        when(differentialExperimentMock.getDataColumnDescriptors()).thenReturn(ImmutableList.of(contrast));
    }

    @Test
    void baselineConditions() {
        assertThat(subject.getConditions(baselineExperimentMock))
                .flatExtracting("values")
                .containsOnlyElementsOf(
                        ImmutableSet.<String>builder()
                            // Commenting out any of the following .addAll statements will make the test fail
                            .addAll(expandedOntologyTermLabels)
                            .addAll(expandedOntologyTerms.values())
                            .addAll(
                                    factorValues.values().stream()
                                            .flatMap(headerValueMap -> headerValueMap.values().stream())
                                            .collect(toSet()))
                            .addAll(
                                    sampleCharacteristics.values().stream()
                                            .flatMap(headerValueMap -> headerValueMap.values().stream())
                                            .collect(toSet()))
                            .build());
    }

    @Test
    void differentialConditions() {
        assertThat(subject.getConditions(differentialExperimentMock))
                .flatExtracting("values")
                .containsOnlyElementsOf(
                        // Commenting out any of the following .addAll statements will make the test fail
                        ImmutableSet.<String>builder()
                                .addAll(expandedOntologyTermLabels)
                                .addAll(
                                        testAssayGroup.getAssayIds().stream()
                                                .flatMap(assayId ->
                                                        ImmutableSet.<String>builder()
                                                                .addAll(expandedOntologyTerms.get(assayId))
                                                                .addAll(factorValues.get(assayId).values())
                                                                .addAll(sampleCharacteristics.get(assayId).values())
                                                                .build()
                                                                .stream())
                                                .collect(toList()))
                                .addAll(
                                        referenceAssayGroup.getAssayIds().stream()
                                                .flatMap(assayId ->
                                                        ImmutableSet.<String>builder()
                                                                .addAll(expandedOntologyTerms.get(assayId))
                                                                .addAll(factorValues.get(assayId).values())
                                                                .addAll(sampleCharacteristics.get(assayId).values())
                                                                .build()
                                                                .stream())
                                                .collect(toList()))
                                .build());
    }

    private static <K, V> Multimap<K, V> randomizedMultimapOf(Collection<K> buckets, Collection<V> values) {
        checkArgument(values.size() >= buckets.size());

        var _buckets = Lists.newArrayList(buckets);
        Collections.shuffle(_buckets);
        var _values = Lists.newArrayList(values);
        Collections.shuffle(_values);

        var builder = ImmutableMultimap.<K, V>builder();

        // First, all buckets should have at least one value
        for (var bucket : _buckets) {
            builder.put(bucket, _values.remove(0));
        }

        // Distribute the remaining values randomly among buckets
        while (!_values.isEmpty()) {
            builder.put(
                    _buckets.get(RNG.nextInt(0, _buckets.size())),
                    _values.remove(0));
        }

        return builder.build();
    }
}
