package uk.ac.ebi.atlas.profiles.stream;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.atlas.experimentpage.context.BulkDifferentialRequestContext;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.DifferentialExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.experiment.differential.Regulation;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.BulkDifferentialProfile;
import uk.ac.ebi.atlas.profiles.IterableObjectInputStream;
import uk.ac.ebi.atlas.testutils.AssayGroupFactory;
import uk.ac.ebi.atlas.testutils.MockDataFileHub;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RnaSeqProfileStreamFactoryTest {
    private MockDataFileHub dataFileHub;
    private BulkDifferentialProfileStreamFactory subject;

    private static final AssayGroup G1 = AssayGroupFactory.create("g1", "assay_1");
    private static final AssayGroup G2 = AssayGroupFactory.create("g2", "assay_2");
    private static final AssayGroup G3 = AssayGroupFactory.create("g3", "assay_3");

    private static final Contrast G1_G2 = new Contrast("g1_g2", "contrast 1", G1, G2, null);
    private static final Contrast G1_G3 = new Contrast("g1_g3", "contrast 2", G1, G3, null);

    private static final DifferentialExperiment EXPERIMENT =
            new DifferentialExperimentBuilder()
                    .withSamples(ImmutableList.of(G1_G2, G1_G3))
                    .build();

    private static final String[] HEADER =
            new String[] {
            "Gene ID", "Gene Name", "g1_g2.p-value", "g1_g2.log2foldchange", "g1_g3.p-value", "g1_g3.log2foldchange"};

    @Before
    public void setUp() throws Exception {
        dataFileHub = MockDataFileHub.create();
        subject = new BulkDifferentialProfileStreamFactory(dataFileHub);
    }

    private void testCaseNoExpressionFilter(List<String[]> dataLines,
                                            Collection<String> getGeneIds,
                                            List<BulkDifferentialProfile> expected) {
        DifferentialRequestPreferences differentialRequestPreferences = new DifferentialRequestPreferences();
        differentialRequestPreferences.setFoldChangeCutoff(0.0);
        differentialRequestPreferences.setCutoff(1.0);
        testCase(dataLines, getGeneIds, expected, differentialRequestPreferences);
    }

    private void testCaseNoCutoff(List<String[]> dataLines,
                                  Regulation regulation,
                                  List<BulkDifferentialProfile> expected) {
        DifferentialRequestPreferences differentialRequestPreferences = new DifferentialRequestPreferences();
        differentialRequestPreferences.setFoldChangeCutoff(0.0);
        differentialRequestPreferences.setCutoff(1.0);
        differentialRequestPreferences.setRegulation(regulation);
        testCase(dataLines, Collections.emptySet(), expected, differentialRequestPreferences);
    }

    private void testCaseFoldChangeCutoff(List<String[]> dataLines,
                                          Double foldChangeCutoff,
                                          List<BulkDifferentialProfile> expected) {
        DifferentialRequestPreferences differentialRequestPreferences = new DifferentialRequestPreferences();
        differentialRequestPreferences.setFoldChangeCutoff(foldChangeCutoff);
        differentialRequestPreferences.setCutoff(1.0);
        testCase(dataLines, Collections.emptySet(), expected, differentialRequestPreferences);
    }

    private void testCasePValueCutoff(List<String[]> dataLines,
                                      Double pValueCutoff,
                                      List<BulkDifferentialProfile> expected) {
        DifferentialRequestPreferences differentialRequestPreferences = new DifferentialRequestPreferences();
        differentialRequestPreferences.setFoldChangeCutoff(0.0);
        differentialRequestPreferences.setCutoff(pValueCutoff);
        testCase(dataLines, Collections.emptySet(), expected, differentialRequestPreferences);
    }

    private void testCase(List<String[]> dataLines,
                          Collection<String> getGeneIds,
                          List<BulkDifferentialProfile> expected,
                          DifferentialRequestPreferences differentialRequestPreferences) {
        dataFileHub.addRnaSeqAnalyticsFile(EXPERIMENT.getAccession(), dataLines);
        assertThat(
                ImmutableList.copyOf(
                        new IterableObjectInputStream<>(
                                subject.create(
                                        EXPERIMENT,
                                        new BulkDifferentialRequestContext(differentialRequestPreferences, EXPERIMENT),
                                        getGeneIds))),
                is(expected));
    }

    private BulkDifferentialProfile profile(String id, String name,
                          DifferentialExpression expressionForG1G2,
                          DifferentialExpression expressionForG1G3) {
        BulkDifferentialProfile profile = new BulkDifferentialProfile(id, name);
        Optional.ofNullable(expressionForG1G2).ifPresent(e -> profile.add(G1_G2, e));
        Optional.ofNullable(expressionForG1G3).ifPresent(e -> profile.add(G1_G3, e));
        return profile;
    }

    @Test
    public void nullCases() {
        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER),
                Collections.emptySet(),
                ImmutableList.of());

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER),
                ImmutableList.of("id_1"),
                ImmutableList.of());

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.05", "2.0", "NA", "NA"}),
                ImmutableList.of("different_id"),
                ImmutableList.of());
    }

    @Test
    public void emptyProfileCases() {
        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "NA", "NA", "NA", "NA"}),
                ImmutableList.of("id_1"),
                ImmutableList.of(profile("id_1", "name_1", null, null)));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.05", "NA", "NA", "NA"}),
                ImmutableList.of("id_1"),
                ImmutableList.of(profile("id_1", "name_1", null, null)));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "NA", "2.0", "NA", "NA"}),
                ImmutableList.of("id_1"),
                ImmutableList.of(profile("id_1", "name_1", null, null)));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "NA", "-2.0", "NA", "NA"}),
                ImmutableList.of("id_1"),
                ImmutableList.of(profile("id_1", "name_1", null, null)));
    }

    @Test
    public void getDataNoCutoff() {
        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "NA", "NA"}),
                Collections.emptySet(),
                ImmutableList.of(profile("id_1", "name_1", new DifferentialExpression(0.01, 2.0), null)));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "NA", "NA"}),
                ImmutableList.of("id_1"),
                ImmutableList.of(profile("id_1", "name_1", new DifferentialExpression(0.01, 2.0), null)));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "NA", "NA", "0.03", "4.0"}),
                Collections.emptySet(),
                ImmutableList.of(profile("id_1", "name_1", null, new DifferentialExpression(0.03, 4.0))));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                Collections.emptySet(),
                ImmutableList.of(
                        profile(
                                "id_1", "name_1",
                                new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0))));

        testCaseNoExpressionFilter(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "-2.0", "NA", "NA"}),
                ImmutableList.of("id_1"),
                ImmutableList.of(profile("id_1", "name_1", new DifferentialExpression(0.01, -2.0), null)));
    }


    @Test
    public void getDataRegulation() {
        testCaseNoCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "-2.0", "0.03", "4.0"}),
                Regulation.UP_DOWN,
                ImmutableList.of(
                        profile(
                                "id_1", "name_1",
                                new DifferentialExpression(0.01, -2.0), new DifferentialExpression(0.03, 4.0))));

        testCaseNoCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "-2.0", "0.03", "4.0"}),
                Regulation.UP,
                ImmutableList.of(profile("id_1", "name_1", null, new DifferentialExpression(0.03, 4.0))));

        testCaseNoCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "-2.0", "0.03", "4.0"}),
                Regulation.DOWN,
                ImmutableList.of(profile("id_1", "name_1", new DifferentialExpression(0.01, -2.0), null)));
    }

    @Test
    public void getDataFoldChangeCutoff() {
        testCaseFoldChangeCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                0.0,
                ImmutableList.of(
                        profile(
                                "id_1", "name_1",
                                new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0))));

        testCaseFoldChangeCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                2.5,
                ImmutableList.of(profile("id_1", "name_1", null, new DifferentialExpression(0.03, 4.0))));

        testCaseFoldChangeCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                4.5,
                ImmutableList.of(profile("id_1", "name_1", null, null)));
    }

    @Test
    public void getDataPValueCutoff() {
        testCasePValueCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                1.0,
                ImmutableList.of(
                        profile(
                                "id_1", "name_1",
                                new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0))));

        testCasePValueCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                0.02,
                ImmutableList.of(profile("id_1", "name_1", new DifferentialExpression(0.01, 2.0), null)));

        testCasePValueCutoff(
                ImmutableList.of(HEADER, new String[] {"id_1", "name_1", "0.01", "2.0", "0.03", "4.0"}),
                0.005,
                ImmutableList.of(profile("id_1", "name_1", null, null)));
    }
}
