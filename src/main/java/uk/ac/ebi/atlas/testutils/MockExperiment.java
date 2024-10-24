package uk.ac.ebi.atlas.testutils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesProperties;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

public class MockExperiment {
    protected MockExperiment() {
        throw new UnsupportedOperationException();
    }

    private static final String SPECIES_NAME =
            "Homo sapiens";
    private static final SpeciesProperties SPECIES_PROPERTIES =
            SpeciesProperties.create("Homo_sapiens", "ORGANISM_PART", "animals", ImmutableList.of());
    private static final String DESCRIPTION =
            "This is the experiment description";
    private static final String DISPLAY_NAME =
            "Experiment Display Name";
    private static final List<String> PUBMEDID =
            singletonList("PUBMEDID");
    private static final List<String> DOI =
            singletonList("100.100/doi");
    private static final List<String> PROVIDER_URL =
            Arrays.asList("http://www.provider.com", "http://www.provider1.com");
    private static final List<String> PROVIDER_DESCRIPTION =
            Arrays.asList("Baseline experiment data provider", "Another baseline experiment data provider");
    private static final String EXPERIMENT_ACCESSION =
            RandomDataTestUtils.generateRandomExperimentAccession();
    private static final ImmutableSet<String> ARRAY_DESIGN_ACCESSIONS =
            ImmutableSet.of("A-AFFY-44", "A-GEOD-20277");
    private static final List<ArrayDesign> ARRAY_DESIGNS =
            ImmutableList.of(
                    ArrayDesign.create("A-AFFY-44", "Affymetrix GeneChip Human Genome U133 Plus 2.0 [HG-U133_Plus_2]"),
                    ArrayDesign.create("A-GEOD-20277", "TaqMan® Array Human MicroRNA A+B Cards Set v3.0"));
    private static final List<AssayGroup> ASSAY_GROUPS =
            ImmutableList.of(
                    AssayGroupFactory.create("g1", "run1"),
                    AssayGroupFactory.create("g2", "run2"));

    private static final List<Contrast> CONTRASTS = ImmutableList.of(
            new Contrast(
                    "g1_g2",
                    "contrast",
                    ASSAY_GROUPS.get(0),
                    ASSAY_GROUPS.get(1),
                    ARRAY_DESIGN_ACCESSIONS.iterator().next()));

    public static BaselineExperiment createBaselineExperiment() {
        return createBaselineExperiment(EXPERIMENT_ACCESSION);
    }

    public static BaselineExperiment createBaselineExperiment(String accession) {
        return createBaselineExperiment(
                accession,
                mockExperimentDesign(ASSAY_GROUPS),
                ASSAY_GROUPS,
                ExperimentDisplayDefaults.create());
    }

    public static BaselineExperiment createBaselineExperiment(List<String> pubmedIds, List<String> dois) {
        return createBaselineExperiment(
                EXPERIMENT_ACCESSION,
                mockExperimentDesign(ASSAY_GROUPS),
                ASSAY_GROUPS,
                ExperimentDisplayDefaults.create(),
                pubmedIds,
                dois);
    }

    public static BaselineExperiment createBaselineExperiment(List<AssayGroup> assayGroups) {
        return createBaselineExperiment(
                mockExperimentDesign(assayGroups),
                assayGroups);
    }

    public static BaselineExperiment createBaselineExperiment(ExperimentDesign experimentDesign,
                                                              List<AssayGroup> assayGroups) {
        return createBaselineExperiment(
                experimentDesign,
                assayGroups,
                ExperimentDisplayDefaults.create());
    }

    public static BaselineExperiment createBaselineExperiment(String accession,
                                                              ExperimentDesign experimentDesign,
                                                              List<AssayGroup> assayGroups) {
        return createBaselineExperiment(
                accession,
                experimentDesign,
                assayGroups,
                ExperimentDisplayDefaults.create());
    }

    public static BaselineExperiment createBaselineExperiment(ExperimentDesign experimentDesign,
                                                              List<AssayGroup> assayGroups,
                                                              ExperimentDisplayDefaults experimentDisplayDefaults) {
        return createBaselineExperiment(
                EXPERIMENT_ACCESSION,
                experimentDesign,
                assayGroups,
                experimentDisplayDefaults);
    }

    public static BaselineExperiment createBaselineExperiment(String accession,
                                                              ExperimentDesign experimentDesign,
                                                              List<AssayGroup> assayGroups,
                                                              ExperimentDisplayDefaults experimentDisplayDefaults) {
        return createBaselineExperiment(
                accession,
                experimentDesign,
                assayGroups,
                experimentDisplayDefaults,
                PUBMEDID, DOI);
    }

    public static BaselineExperiment createBaselineExperiment(String accession,
                                                              ExperimentDesign experimentDesign,
                                                              List<AssayGroup> assayGroups,
                                                              ExperimentDisplayDefaults experimentDisplayDefaults,
                                                              List<String> pubmedIds,
                                                              List<String> dois) {

        var experimentalFactorHeaders = ImmutableSet.copyOf(experimentDesign.getFactorHeaders());
        var assayId2Factor = ImmutableMap.copyOf(experimentDesign.getAssayId2FactorMap());

        return new BaselineExperiment(
                RNASEQ_MRNA_BASELINE,
                accession,
                ImmutableSet.of(),
                DESCRIPTION,
                new Date(),
                new Date(),
                new Species(SPECIES_NAME, SPECIES_PROPERTIES),
                ImmutableList.of("technologyType"),
                assayGroups,
                experimentalFactorHeaders,
                Sets.newHashSet(pubmedIds),
                Sets.newHashSet(dois),
                DISPLAY_NAME,
                "",
                PROVIDER_URL,
                PROVIDER_DESCRIPTION,
                emptyList(),
                emptyList(),
                experimentDisplayDefaults,
                false,
                UUID.randomUUID().toString(),
                assayId2Factor
        );
    }

    public static MicroarrayExperiment createMicroarrayExperiment() {

        var experimentDesign = mockExperimentDesign(ASSAY_GROUPS);
        var experimentalFactorHeaders = ImmutableSet.copyOf(experimentDesign.getFactorHeaders());

        return new MicroarrayExperiment(
                MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL,
                EXPERIMENT_ACCESSION,
                ImmutableSet.of(),
                DESCRIPTION,
                new Date(),
                new Date(),
                new Species(SPECIES_NAME, SPECIES_PROPERTIES),
                ImmutableList.of("technologyType"),
                CONTRASTS.stream().map(contrast1 -> Pair.of(contrast1, true)).collect(Collectors.toList()),
                experimentalFactorHeaders,
                Sets.newHashSet(PUBMEDID),
                Sets.newHashSet(DOI),
                ARRAY_DESIGNS,
                false,
                UUID.randomUUID().toString());
    }

    public static DifferentialExperiment createDifferentialExperiment() {
        var experimentDesign = mockExperimentDesign(ASSAY_GROUPS);
        var experimentalFactorHeaders = ImmutableSet.copyOf(experimentDesign.getFactorHeaders());

        return new DifferentialExperiment(
                RNASEQ_MRNA_DIFFERENTIAL,
                EXPERIMENT_ACCESSION,
                ImmutableSet.of(),
                DESCRIPTION,
                new Date(),
                new Date(),
                new Species(SPECIES_NAME, SPECIES_PROPERTIES),
                ImmutableList.of("technologyType"),
                CONTRASTS.stream().map(contrast1 -> Pair.of(contrast1, true)).collect(Collectors.toList()),
                experimentalFactorHeaders,
                Sets.newHashSet(PUBMEDID),
                Sets.newHashSet(DOI),
                false,
                UUID.randomUUID().toString());
    }

    public static DifferentialExperiment createDifferentialExperiment(String accession, List<Contrast> contrasts) {
        return createDifferentialExperiment(
                accession,
                contrasts,
                mockExperimentDesign(
                        contrasts.stream()
                                .flatMap(contrast ->
                                        Stream.of(contrast.getTestAssayGroup(), contrast.getReferenceAssayGroup()))
                                .collect(toList())));
    }

    public static DifferentialExperiment createDifferentialExperiment(String accession,
                                                                      List<Contrast> contrasts,
                                                                      ExperimentDesign experimentDesign) {
        var experimentalFactorHeaders = ImmutableSet.copyOf(experimentDesign.getFactorHeaders());

        return new DifferentialExperiment(
                RNASEQ_MRNA_DIFFERENTIAL,
                accession,
                ImmutableSet.of(),
                "description",
                new Date(),
                new Date(),
                generateRandomSpecies(),
                ImmutableList.of("technologyType"),
                contrasts.stream().map(contrast -> Pair.of(contrast, true)).collect(toList()),
                experimentalFactorHeaders,
                Sets.newHashSet(PUBMEDID),
                Sets.newHashSet(DOI),
                false,
                UUID.randomUUID().toString());
    }

    public static ExperimentDesign mockExperimentDesign(List<AssayGroup> assayGroups) {
        ExperimentDesign experimentDesign = new ExperimentDesign();
        for (AssayGroup assayGroup : assayGroups) {
            String value1 = RandomStringUtils.random(5);
            String value2 = RandomStringUtils.random(5);
            for (String assay : assayGroup.getAssayIds()) {
                experimentDesign.putFactor(assay, "type1", value1);
                experimentDesign.putFactor(assay, "type2", value2);
            }
        }
        return experimentDesign;
    }
}
