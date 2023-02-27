package uk.ac.ebi.atlas.experimentpage.context;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorSet;
import uk.ac.ebi.atlas.testutils.MockExperiment;
import uk.ac.ebi.atlas.web.BaselineRequestPreferencesTest;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroups;

// <defaultFilterFactors> contains a set of factors with values, these will be the initial filters set for the
// experiment page. The defaultQueryFactorType then gets all the values available in that selection.
// For instance, this is the factors XML file for E-MTAB-5200-factors.xml:
//
// <factors-definition>
//    <defaultFilterFactors>
//        <filterFactor>
//            <type>ORGANISM_PART</type>
//            <value>blood</value>
//        </filterFactor>
//    </defaultFilterFactors>
//    <defaultQueryFactorType>DISEASE</defaultQueryFactorType>
//    <menuFilterFactorTypes>DISEASE, ORGANISM_PART</menuFilterFactorTypes>
//    <landingPageDisplayName>Tissues - Pan-Cancer Analysis of Whole Genomes</landingPageDisplayName>
//    <speciesMapping />
//    <orderFactor>curated</orderFactor>
//    <dataProviderURL>https://dcc.icgc.org/pcawg</dataProviderURL>
//    <dataProviderDescription>The Pan-Cancer Analysis of Whole Genomes project</dataProviderDescription>
//    <alternativeView>E-MTAB-5423</alternativeView>
//    <disclaimer>pcawg</disclaimer>
// </factors-definition>
//
// The experiment will be showing the slice corresponding to all assay groups annotated with blood (as per the
// file E-MTAB-5200-configuration.xml). The defaultQueryFactorType (DISEASE) is initialised to the diseases
// corresponding to the blood samples:
// B-cell non-Hodgkin lymphoma
// chronic lymphocytic leukemia
// lymphoma
// normal - blood (GTEx)
class BaselineRequestContextTest {
    // RNG is a random number generator using LocalCurrentThread
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    private static final int MIN_FACTOR_TYPES = 2;
    private static final int MAX_FACTOR_TYPES = 5;
    private static final int MIN_FACTOR_VALUES = 2;
    private static final int MAX_FACTOR_VALUES = 10;
    private static final int MAX_ASSAY_GROUPS = 100;

    // Given a collection of at least size n, partition it into n lists of random size of at least 1 element
    private static <T> ImmutableList<ImmutableList<T>> randomPartition(Collection<T> collection, int n) {
        checkArgument(collection.size() >= n);

        // Crate a copy of the collection and shuffle it
        var collectionCopy = Lists.newArrayList(collection);
        Collections.shuffle(collectionCopy);

        // Crate a list of n immutable list builders
        var partitionBuilders = IntStream.range(0, n)
                .mapToObj(__ -> ImmutableList.<T>builder())
                .collect(toImmutableList());

        // For each list builder take out a random element of the collection and add it to the list builder
        partitionBuilders.forEach(
                builder -> builder.add(collectionCopy.remove(0)));

        // Assign the remaining elements of the collection to the list builders randomly
        collectionCopy.forEach(
                element -> partitionBuilders.get(RNG.nextInt(partitionBuilders.size())).add(element));

        // Build the immutable lists and return them
        return partitionBuilders.stream()
                .map(ImmutableList.Builder::build)
                .collect(toImmutableList());
    }

    // Method that takes some factor types, list of their values, and a set of assay groups, and returns an
    // experiment design that randomly assigns factor values to the assays in the assay groups
    private static ExperimentDesign generateRandomExperimentDesign(
            ImmutableMap<String, ImmutableSet<String>> factorTypeToFactorTypeValues,
            ImmutableCollection<AssayGroup> assayGroups) {
        var experimentDesign = new ExperimentDesign();

        factorTypeToFactorTypeValues.forEach((factorType, factorTypeValues) -> {
            var factorTypeValuesList = factorTypeValues.stream().collect(toImmutableList());
            // Partition assayGroups in as many partitions as factor type values
            var assayGroupsPartition = randomPartition(assayGroups, factorTypeValues.size());
            checkArgument(assayGroupsPartition.size() == factorTypeValuesList.size());

            // You can do this with Streams.zip, but IMHO it’s not as readable, but you need to collect
            // factorTypeValues to a list to so that you can use .get(i)
            for (int i = 0; i < assayGroupsPartition.size(); i++) {
                var factorTypeValue = factorTypeValuesList.get(i);
                var assayGroupPartition = assayGroupsPartition.get(i);
                assayGroupPartition.stream()
                        .map(AssayGroup::getAssayIds)
                        .flatMap(Collection::stream)
                        .forEach(assayId -> experimentDesign.putFactor(assayId, factorType, factorTypeValue));
            }
        });

        return experimentDesign;
    }

    @Test
    void singleFactorTypeExperimentHasFactorValuesAsLabels() {
        // Generate a defaultQueryFactorType and between two and ten values for it
        var defaultQueryFactorType = randomAlphabetic(20);
        var defaultQueryFactorTypeValues = IntStream.range(0, RNG.nextInt(MIN_FACTOR_VALUES, MAX_FACTOR_VALUES)).boxed()
                .map(__ -> randomAlphabetic(10))
                .collect(toImmutableSet());

        // assayGroups is a list of random assay groups; there should be at least as many assay groups as factor values
        var assayGroups =
                generateRandomAssayGroups(RNG.nextInt(defaultQueryFactorTypeValues.size(), MAX_ASSAY_GROUPS));

        // Create an experiment design that randomly assigns factor values to assay groups
        var experimentDesign = generateRandomExperimentDesign(
                ImmutableMap.of(defaultQueryFactorType, defaultQueryFactorTypeValues),
                assayGroups);

        var subject =  new BaselineRequestContext<>(
                BaselineRequestPreferencesTest.get(),
                MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));

        // The display names for the column should be the factor values
        assertThat(assayGroups)
                .allSatisfy(
                        assayGroup ->
                                assertThat(subject.displayNameForColumn(assayGroup))
                                        .isEqualToIgnoringCase(
                                                experimentDesign.getFactorValue(
                                                        assayGroup.getFirstAssayId(), defaultQueryFactorType)));
    }

    @Test
    void multipleFactorTypeExperimentColumnsAreLabelledWithFactorValuesJoinedWithCommas() {
        // Create a map of factor types to factor type values; the number of factor types is between 2 and 5;
        // the factor values are between 2 and 10
        var factorTypesToFactorTypeValues = IntStream.range(0, RNG.nextInt(MIN_FACTOR_TYPES, MAX_FACTOR_TYPES)).boxed()
                .collect(toImmutableMap(
                        __ -> randomAlphabetic(10),
                        __ -> IntStream.range(0, RNG.nextInt(MIN_FACTOR_VALUES, MAX_FACTOR_VALUES)).boxed()
                                .map(___ -> randomAlphabetic(10))
                                .collect(toImmutableSet())));

        // Max number of a factor type values
        var maxFactorTypeValues = factorTypesToFactorTypeValues.values().stream()
                .map(ImmutableSet::size)
                .max(Comparator.naturalOrder())
                .orElse(0);

        // assayGroups is a list of random assay groups; there should be at least as many assay groups as factor values
        var assayGroups = generateRandomAssayGroups(RNG.nextInt(maxFactorTypeValues, MAX_ASSAY_GROUPS));

        // Create an experiment design that randomly assigns factor values to assay groups
        var experimentDesign = generateRandomExperimentDesign(factorTypesToFactorTypeValues, assayGroups);

        var subject = new BaselineRequestContext<>(
                BaselineRequestPreferencesTest.get(),
                MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));


        assertThat(assayGroups)
                .allSatisfy(
                        assayGroup -> {
                            assertThat(subject.displayNameForColumn(assayGroup).split(", "))
                                    .hasSameSizeAs(factorTypesToFactorTypeValues.keySet());
                            factorTypesToFactorTypeValues.keySet().forEach(
                                    factorType ->
                                            assertThat(subject.displayNameForColumn(assayGroup))
                                                    .contains(
                                                            experimentDesign.getFactorValue(
                                                                    assayGroup.getFirstAssayId(), factorType)));
                        });
    }

    @Test
    void emptyFactorTypeValuesAreRemoved() {
        // Create a map of factor types to factor type values; the number of factor types is between 2 and 5;
        // the factor values are between 2 and 10
        var factorTypesToFactorTypeValues = IntStream.range(0, RNG.nextInt(MIN_FACTOR_TYPES, MAX_FACTOR_TYPES)).boxed()
                .collect(toImmutableMap(
                        __ -> randomAlphabetic(10),
                        __ -> ImmutableSet.<String>builder().addAll(
                                IntStream.range(0, RNG.nextInt(MIN_FACTOR_VALUES, MAX_FACTOR_VALUES)).boxed()
                                        .map(___ -> randomAlphabetic(10))
                                        .collect(toImmutableSet()))
                                .add(generateBlankString())
                                .build()));


        // Max number of a factor type values
        var maxFactorTypeValues = factorTypesToFactorTypeValues.values().stream()
                .map(ImmutableSet::size)
                .max(Comparator.naturalOrder())
                .orElse(0);

        // assayGroups is a list of random assay groups; there should be at least as many assay groups as factor values
        var assayGroups = generateRandomAssayGroups(RNG.nextInt(maxFactorTypeValues, MAX_ASSAY_GROUPS));

        // Create an experiment design that randomly assigns factor values to assay groups
        var experimentDesign = generateRandomExperimentDesign(factorTypesToFactorTypeValues, assayGroups);

        var subject =  new BaselineRequestContext<>(
                BaselineRequestPreferencesTest.get(),
                MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));


        var assayGroupsWithEmptyFactorValues = assayGroups.stream()
                .filter(assayGroup -> {
                    var factorSpliterator = spliteratorUnknownSize(
                            experimentDesign.getFactors(assayGroup.getFirstAssayId()).iterator(),
                            ORDERED);
                    return stream(factorSpliterator, false).anyMatch(factor -> factor.getValue().isEmpty());
                })
                .collect(toImmutableSet());

        assertThat(assayGroupsWithEmptyFactorValues)
                .allSatisfy(
                        assayGroup -> {
                            assertThat(subject.displayNameForColumn(assayGroup).split(", ").length)
                                    .isLessThan(factorTypesToFactorTypeValues.keySet().size());
                        });
    }

//    @Test
//    public void multiFactorExperimentWhereDisplayedColumnsShareAFactorShowsOnlyTheDifferentPart() {
//        var defaultQueryFactorType = "defaultQueryFactorType";
//        var otherType = "otherQueryFactorType";
//
//        var assayGroups = ImmutableList.of(generateRandomAssayGroup(), generateRandomAssayGroup());
//
//        var experimentDesign = mock(ExperimentDesign.class);
//
//        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), defaultQueryFactorType, "liver");
//        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), otherType, "foo");
//
//        FactorSet factors1 = getFactors(defaultQueryFactorType, otherType);
//
//        when(experimentDesign.getFactors(assayGroups.get(0).getFirstAssayId())).thenReturn(factors1);
//
//        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), defaultQueryFactorType, "heart");
//        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), otherType, "foo");
//
//        var factors2 = new FactorSet();
//        factors2.add(new Factor(defaultQueryFactorType, "heart"));
//        factors2.add(new Factor(otherType, "foo"));
//
//        when(experimentDesign.getFactors(assayGroups.get(1).getFirstAssayId())).thenReturn(factors2);
//
//        var subject =  new BaselineRequestContext<>(
//                        BaselineRequestPreferencesTest.get(),
//                        MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));
//
//        assertThat(subject.displayNameForColumn(assayGroups.get(0)), (is("liver")));
//        assertThat(subject.displayNameForColumn(assayGroups.get(1)), (is("heart")));
//    }
//
//    @Test
//    public void whenViewIsAllFlatAndAllFactorsDifferWeShowThemInPrescribedOrder() {
//        var defaultQueryFactorType = "defaultQueryFactorType";
//        var otherType = "otherQueryFactorType";
//
//        var defaultFactorValues = ImmutableSet.of(new Factor(otherType, "defaultValueForOtherType"));
//        var prescribedOrderOfFilters = ImmutableList.of(defaultQueryFactorType, otherType);
//
//        var assayGroups = ImmutableList.of(generateRandomAssayGroup(), generateRandomAssayGroup());
//
//        var experimentDesign = mock(ExperimentDesign.class);
//
//        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), defaultQueryFactorType, "liver");
//        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), otherType, "foo");
//
//        var factors1 = getFactors(defaultQueryFactorType, otherType);
//
//        when(experimentDesign.getFactors(assayGroups.get(0).getFirstAssayId())).thenReturn(factors1);
//
//        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), defaultQueryFactorType, "heart");
//        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), otherType, "bar");
//
//        var factors2 = new FactorSet();
//        factors2.add(new Factor(defaultQueryFactorType, "heart"));
//        factors2.add(new Factor(otherType, "bar"));
//
//        when(experimentDesign.getFactors(assayGroups.get(1).getFirstAssayId())).thenReturn(factors2);
//
//        var subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
//                        MockExperiment.createBaselineExperiment(
//                        experimentDesign,
//                        assayGroups,
//                        ExperimentDisplayDefaults.create(
//                                defaultQueryFactorType,
//                                defaultFactorValues,
//                                prescribedOrderOfFilters,
//                                false)));
//
//        assertThat(subject.displayNameForColumn(assayGroups.get(0)), (is("liver, foo")));
//        assertThat(subject.displayNameForColumn(assayGroups.get(1)), (is("heart, bar")));
//
//        subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
//                        MockExperiment.createBaselineExperiment(
//                        experimentDesign,
//                        assayGroups,
//                        ExperimentDisplayDefaults.create(
//                                defaultQueryFactorType,
//                                defaultFactorValues,
//                                Lists.reverse(prescribedOrderOfFilters),
//                                false)));
//
//        assertThat(subject.displayNameForColumn(assayGroups.get(0)), (is("foo, liver")));
//        assertThat(subject.displayNameForColumn(assayGroups.get(1)), (is("bar, heart")));
//    }

//    @Test
//    public void whenSomeTypesAreTheSameAcrossTheSetTheyDoNotGoIntoTheName() {
//        var defaultQueryFactorType = "DISEASE";
//        var filterFactorType = "ORGANISM_PART";
//
//        var defaultFilterFactors = ImmutableSet.of(new Factor(filterFactorType, "blood"));
//        var prescribedOrderOfFactors = ImmutableList.of(defaultQueryFactorType, filterFactorType);
//
//        var assayGroups = ImmutableList.of(generateRandomAssayGroup(), generateRandomAssayGroup());
//
//        var experimentDesign = mock(ExperimentDesign.class);
//
////        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), defaultQueryFactorType, "liver");
////        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), otherType, "foo");
//
//        var factorsForFirstAssayGroup = new FactorSet();
//        factorsForFirstAssayGroup.add(new Factor(defaultQueryFactorType, "lymphoma"));
//        factorsForFirstAssayGroup.add(new Factor(filterFactorType, "foo1"));
//
//        when(experimentDesign.getFactors(assayGroups.get(0).getFirstAssayId())).thenReturn(factorsForFirstAssayGroup);
//
////        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), defaultQueryFactorType, "heart");
////        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), otherType, "foo");
//
//        var factorsForSecondAssayGroup = new FactorSet();
//        factorsForSecondAssayGroup.add(new Factor(defaultQueryFactorType, "normal - blood (GTEx)"));
//        factorsForSecondAssayGroup.add(new Factor(filterFactorType, "foo2"));
//
//        when(experimentDesign.getFactors(assayGroups.get(1).getFirstAssayId())).thenReturn(factorsForSecondAssayGroup);
//
//        when(experimentDesign.getFactorHeaders()).thenReturn(ImmutableSet.of(defaultQueryFactorType, filterFactorType));
//        when(experimentDesign.getAssayId2FactorMap()).thenReturn(
//                ImmutableMap.<String, FactorSet>builder()
//                        .putAll(getAssayId2FactorSetMap(assayGroups.get(0), factorsForFirstAssayGroup))
//                        .putAll(getAssayId2FactorSetMap(assayGroups.get(1), factorsForSecondAssayGroup))
//                        .build());
//
//        var subject = new BaselineRequestContext<>(
//                BaselineRequestPreferencesTest.get(),
//                MockExperiment.createBaselineExperiment(
//                        experimentDesign,
//                        assayGroups,
//                        ExperimentDisplayDefaults.create(
//                                defaultQueryFactorType,
//                                defaultFilterFactors,
//                                prescribedOrderOfFactors,
//                                // <orderFactor>curated</orderFactor> means that the factors will be ordered
//                                true)));
//
//        // "foo" is removed from the display name because it is in all assay groups
//        assertThat(subject.displayNameForColumn(assayGroups.get(0)), (is("lymphoma")));
//        assertThat(subject.displayNameForColumn(assayGroups.get(1)), (is("normal - blood (GTEx)")));
////
////        subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
////                MockExperiment.createBaselineExperiment(
////                        experimentDesign,
////                        assayGroups,
////                        ExperimentDisplayDefaults.create(
////                                defaultQueryFactorType,
////                                defaultFactorValues,
////                                Lists.reverse(prescribedOrderOfFilters),
////                                false)));
////
////        // "foo" is removed from the display name because it is in all assay groups
////        assertThat(subject.displayNameForColumn(assayGroups.get(0)), (is("liver")));
////        assertThat(subject.displayNameForColumn(assayGroups.get(1)), (is("heart")));
//    }

//    @Test
//    public void filterOutFactorTypeEmptyValuesTheyDoNotGoIntoTheName() {
//        var defaultQueryFactorType = "defaultQueryFactorType";
//        var otherType = "otherQueryFactorType";
//
//        var assayGroups = ImmutableList.of(generateRandomAssayGroup(), generateRandomAssayGroup());
//
//        var experimentDesign = mock(ExperimentDesign.class);
//
//        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), defaultQueryFactorType, "liver");
//        experimentDesign.putFactor(assayGroups.get(0).getFirstAssayId(), otherType, generateBlankString());
//
//        var factors1 = new FactorSet();
//        factors1.add(new Factor(defaultQueryFactorType, "liver"));
//        factors1.add(new Factor(otherType, generateBlankString()));
//
//        when(experimentDesign.getFactors(assayGroups.get(0).getFirstAssayId())).thenReturn(factors1);
//
//        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), defaultQueryFactorType, "heart");
//        experimentDesign.putFactor(assayGroups.get(1).getFirstAssayId(), otherType, "foo");
//
//        var factors2 = new FactorSet();
//        factors2.add(new Factor(defaultQueryFactorType, "heart"));
//        factors2.add(new Factor(otherType, "foo"));
//
//        when(experimentDesign.getFactors(assayGroups.get(1).getFirstAssayId())).thenReturn(factors2);
//
//        var subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
//          MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));
//
//        assertThat(subject.displayNameForColumn(assayGroups.get(0)), (is("liver")));
//        assertThat(subject.displayNameForColumn(assayGroups.get(1)), (is("heart, foo")));
//    }

//    private FactorSet getFactors(String defaultQueryFactorType, String otherType) {
//        var factors = new FactorSet();
//        factors.add(new Factor(defaultQueryFactorType, "liver"));
//        factors.add(new Factor(otherType, "foo"));
//        return factors;
//    }

    // Method that takes an assay group and a factor set and returns an immutable map of the assay IDs in the assay
    // group mapped to the factor set, use streams to do this
    private ImmutableMap<String, FactorSet> getAssayId2FactorSetMap(AssayGroup assayGroup, FactorSet factors) {
        return assayGroup.getAssayIds().stream()
                .collect(toImmutableMap(assayId -> assayId, __ -> factors));
    }
}