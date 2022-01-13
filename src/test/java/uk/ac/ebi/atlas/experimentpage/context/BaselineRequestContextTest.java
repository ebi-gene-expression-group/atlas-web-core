package uk.ac.ebi.atlas.experimentpage.context;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.Test;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorSet;
import uk.ac.ebi.atlas.testutils.AssayGroupFactory;
import uk.ac.ebi.atlas.testutils.MockExperiment;
import uk.ac.ebi.atlas.web.BaselineRequestPreferencesTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaselineRequestContextTest {

    @Test
    public void singleFactorExperimentHasSimpleLabels() {
        var defaultQueryFactorType = "defaultQueryFactorType";

        var ag1 = AssayGroupFactory.create("g1", "run11");
        var ag2 = AssayGroupFactory.create("g2", "run21");
        var assayGroups = ImmutableList.of(ag1, ag2);

        var experimentDesign = mock(ExperimentDesign.class);

        var factors1 = new FactorSet();
        factors1.add(new Factor(defaultQueryFactorType, "name for g1"));

        when(experimentDesign.getFactors("run11")).thenReturn(factors1);

        var factors2 = new FactorSet();
        factors2.add(new Factor(defaultQueryFactorType, "name for g2"));

        when(experimentDesign.getFactors("run21")).thenReturn(factors2);

        var subject = new BaselineRequestContext<>(
                        BaselineRequestPreferencesTest.get(),
                        MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));

        assertThat(subject.displayNameForColumn(ag1), (is("name for g1")));
        assertThat(subject.displayNameForColumn(ag2), (is("name for g2")));
    }

    @Test
    public void multiFactorExperimentWhereDisplayedColumnsShareAFactorShowsOnlyTheDifferentPart() {
        var defaultQueryFactorType = "defaultQueryFactorType";
        var otherType = "otherQueryFactorType";

        var ag1 = AssayGroupFactory.create("g1", "run11");
        var ag2 = AssayGroupFactory.create("g2", "run21");
        var assayGroups = ImmutableList.of(ag1, ag2);

        var experimentDesign = mock(ExperimentDesign.class);

        var factors1 = new FactorSet();
        factors1.add(new Factor(defaultQueryFactorType, "name for g1"));
        factors1.add(new Factor(otherType, "otherTypeValue"));

        when(experimentDesign.getFactors("run11")).thenReturn(factors1);

        var factors2 = new FactorSet();
        factors2.add(new Factor(defaultQueryFactorType, "name for g2"));
        factors2.add(new Factor(otherType, "otherTypeValue"));

        when(experimentDesign.getFactors("run21")).thenReturn(factors2);

        var subject =  new BaselineRequestContext<>(
                        BaselineRequestPreferencesTest.get(),
                        MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));

        assertThat(subject.displayNameForColumn(ag1), (is("name for g1")));
        assertThat(subject.displayNameForColumn(ag2), (is("name for g2")));
    }

    @Test
    public void whenViewIsAllFlatAndAllFactorsDifferWeShowThemInPrescribedOrder() {
        var defaultQueryFactorType = "defaultQueryFactorType";
        var otherType = "otherQueryFactorType";

        var defaultFactorValues = ImmutableSet.of(new Factor(otherType, "defaultValueForOtherType"));
        var prescribedOrderOfFilters = ImmutableList.of(defaultQueryFactorType, otherType);

        var ag1 = AssayGroupFactory.create("g1", "run11");
        var ag2 = AssayGroupFactory.create("g2", "run21");
        var assayGroups = ImmutableList.of(ag1, ag2);

        var experimentDesign = mock(ExperimentDesign.class);

        var factors1 = new FactorSet();
        factors1.add(new Factor(defaultQueryFactorType, "name for g1"));
        factors1.add(new Factor(otherType, "other type value 1"));

        when(experimentDesign.getFactors("run11")).thenReturn(factors1);

        var factors2 = new FactorSet();
        factors2.add(new Factor(defaultQueryFactorType, "name for g2"));
        factors2.add(new Factor(otherType, "other type value 2"));

        when(experimentDesign.getFactors("run21")).thenReturn(factors2);

        var subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
                        MockExperiment.createBaselineExperiment(
                        experimentDesign,
                        assayGroups,
                        ExperimentDisplayDefaults.create(
                                defaultQueryFactorType,
                                defaultFactorValues,
                                prescribedOrderOfFilters,
                                false)));

        assertThat(subject.displayNameForColumn(ag1), (is("name for g1, other type value 1")));
        assertThat(subject.displayNameForColumn(ag2), (is("name for g2, other type value 2")));

        subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
                        MockExperiment.createBaselineExperiment(
                        experimentDesign,
                        assayGroups,
                        ExperimentDisplayDefaults.create(
                                defaultQueryFactorType,
                                defaultFactorValues,
                                Lists.reverse(prescribedOrderOfFilters),
                                false)));

        assertThat(subject.displayNameForColumn(ag1), (is("other type value 1, name for g1")));
        assertThat(subject.displayNameForColumn(ag2), (is("other type value 2, name for g2")));
    }

    @Test
    public void whenSomeTypesAreTheSameAcrossTheSetTheyDoNotGoIntoTheName() {
        var defaultQueryFactorType = "defaultQueryFactorType";
        var otherType = "otherQueryFactorType";

        var defaultFactorValues = ImmutableSet.of(new Factor(otherType, "defaultValueForOtherType"));
        var prescribedOrderOfFilters = ImmutableList.of(defaultQueryFactorType, otherType);

        var ag1 = AssayGroupFactory.create("g1", "run11");
        var ag2 = AssayGroupFactory.create("g2", "run21");
        var assayGroups = ImmutableList.of(ag1, ag2);

        var experimentDesign = mock(ExperimentDesign.class);

        var factors1 = new FactorSet();
        factors1.add(new Factor(defaultQueryFactorType, "name for g1"));
        factors1.add(new Factor(otherType, "other type value"));

        when(experimentDesign.getFactors("run11")).thenReturn(factors1);

        var factors2 = new FactorSet();
        factors2.add(new Factor(defaultQueryFactorType, "name for g2"));
        factors2.add(new Factor(otherType, "other type value"));

        when(experimentDesign.getFactors("run21")).thenReturn(factors2);

        var subject = new BaselineRequestContext<>(
                        BaselineRequestPreferencesTest.get(),
                        MockExperiment.createBaselineExperiment(
                        experimentDesign,
                        assayGroups,
                        ExperimentDisplayDefaults.create(
                                defaultQueryFactorType,
                                defaultFactorValues,
                                prescribedOrderOfFilters,
                                false)));

        assertThat(subject.displayNameForColumn(ag1), (is("name for g1")));
        assertThat(subject.displayNameForColumn(ag2), (is("name for g2")));

        subject = new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
                        MockExperiment.createBaselineExperiment(
                        experimentDesign,
                        assayGroups,
                        ExperimentDisplayDefaults.create(
                                defaultQueryFactorType,
                                defaultFactorValues,
                                Lists.reverse(prescribedOrderOfFilters),
                                false)));

        assertThat(subject.displayNameForColumn(ag1), (is("name for g1")));
        assertThat(subject.displayNameForColumn(ag2), (is("name for g2")));
    }

    @Test
    public void filterOutFactorTypeEmptyValuesTheyDoNotGoIntoTheName() {
        var defaultQueryFactorType = "defaultQueryFactorType";
        var otherType = "otherQueryFactorType";

        var ag1 = AssayGroupFactory.create("g1", "run11");
        var ag2 = AssayGroupFactory.create("g2", "run21");
        var ag3 = AssayGroupFactory.create("g3", "run22");
        var assayGroups = ImmutableList.of(ag1, ag2, ag3);

        var experimentDesign = mock(ExperimentDesign.class);

        var factors1 = new FactorSet();
        factors1.add(new Factor(defaultQueryFactorType, "name for g1"));
        factors1.add(new Factor(otherType, ""));

        when(experimentDesign.getFactors("run11")).thenReturn(factors1);

        var factors2 = new FactorSet();
        factors2.add(new Factor(defaultQueryFactorType, "name for g2"));
        factors2.add(new Factor(otherType, "other type value 2"));

        when(experimentDesign.getFactors("run21")).thenReturn(factors2);

        var factors3 = new FactorSet();
        factors3.add(new Factor(defaultQueryFactorType, "name for g3"));
        factors3.add(new Factor(otherType, ""));

        when(experimentDesign.getFactors("run22")).thenReturn(factors3);

        BaselineRequestContext<ExpressionUnit.Absolute.Rna> subject =
            new BaselineRequestContext<>(BaselineRequestPreferencesTest.get(),
                MockExperiment.createBaselineExperiment(experimentDesign, assayGroups));

        assertThat(subject.displayNameForColumn(ag1), (is("name for g1")));
        assertThat(subject.displayNameForColumn(ag2), (is("name for g2, other type value 2")));
        assertThat(subject.displayNameForColumn(ag3), (is("name for g3")));
    }
}
