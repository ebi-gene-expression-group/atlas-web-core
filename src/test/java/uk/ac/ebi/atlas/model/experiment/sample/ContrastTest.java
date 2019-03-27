package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomContrast;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomRnaSeqRunId;

class ContrastTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void throwIfIdAnyFieldButArrayDesignIsNull() {
        Contrast contrast = generateRandomContrast(false);
        assertThat(new Contrast(
                        contrast.getId(),
                        contrast.getDisplayName(),
                        contrast.getReferenceAssayGroup(),
                        contrast.getTestAssayGroup(),
                        null))
                .hasFieldOrPropertyWithValue("arrayDesignAccession", null);
    }

    @Test
    void throwIfReferenceAssayGroupEqualsTestAssayGroup() {
        Contrast subject = generateRandomContrast(RNG.nextBoolean());

        assertThatIllegalArgumentException().isThrownBy(
                () -> new Contrast(
                        subject.getId(),
                        subject.getDisplayName(),
                        subject.getReferenceAssayGroup(),
                        new AssayGroup(
                                subject.getReferenceAssayGroup().getId(),
                                subject.getTestAssayGroup().getAssays()),
                        subject.getArrayDesignAccession()));
    }

    @Test
    void throwIfReferenceAssayGroupSharesAssaysWithTestAssayGroup() {
        Contrast subject = generateRandomContrast(RNG.nextBoolean());

        ImmutableList<String> referenceAssayIds = subject.getReferenceAssayGroup().getAssayIds().asList();
        String randomReferenceAssayId = referenceAssayIds.get(RNG.nextInt(0, referenceAssayIds.size()));

        assertThatIllegalArgumentException().isThrownBy(
                () -> new Contrast(
                        subject.getId(),
                        subject.getDisplayName(),
                        subject.getReferenceAssayGroup(),
                        new AssayGroup(
                                subject.getTestAssayGroup().getId(),
                                // Change randomReferenceAssayId to any other value to make the test fail
                                ImmutableSet.of(BiologicalReplicate.create(randomReferenceAssayId))),
                        subject.getArrayDesignAccession()));
    }

    @Test
    void throwIfReferenceAssayGroupSharesTechnicalReplicateGroupIdWithTestAssayGroup() {
        Contrast subject = generateRandomContrast(RNG.nextBoolean());

        BiologicalReplicate technicalReplicate =
                subject.getReferenceAssayGroup().getAssays().stream()
                        .filter(replicate -> replicate.getAssayIds().size() > 1)
                        .findAny()
                        .orElseThrow(RuntimeException::new);

        assertThatIllegalArgumentException().isThrownBy(
                () -> new Contrast(
                        subject.getId(),
                        subject.getDisplayName(),
                        subject.getReferenceAssayGroup(),
                        new AssayGroup(
                                subject.getTestAssayGroup().getId(),
                                ImmutableSet.of(
                                        BiologicalReplicate.create(
                                                // Change replicate ID to any other value to make the test fail
                                                technicalReplicate.getId(),
                                                ImmutableSet.of(
                                                        generateRandomRnaSeqRunId(),
                                                        generateRandomRnaSeqRunId())))),
                        subject.getArrayDesignAccession()));
    }

    @Test
    void throwIfArrayDesignAccessionIsEmpty() {
        Contrast contrast = generateRandomContrast(false);
        String empty = generateBlankString();
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Contrast(
                        contrast.getId(),
                        contrast.getDisplayName(),
                        contrast.getReferenceAssayGroup(),
                        contrast.getTestAssayGroup(),
                        empty));
    }

    @Test
    void compareToIsConsistentWithEquals() {
        Contrast subject = generateRandomContrast(false);

        Contrast thatContrast = subject;
        assertThat(subject.compareTo(thatContrast) == 0)
                .isEqualTo(subject.equals(thatContrast));

        Contrast tmp = generateRandomContrast(true);
        thatContrast = new Contrast(
                tmp.getId(),
                subject.getDisplayName(),
                tmp.getReferenceAssayGroup(),
                tmp.getTestAssayGroup(),
                tmp.getArrayDesignAccession());
        assertThat(subject.compareTo(thatContrast) == 0)
                .isEqualTo(subject.equals(thatContrast));
    }

    @Test
    void notEqualsToOtherClasses() {
        Contrast subject = generateRandomContrast(false);
        assertThat(subject)
                .isNotEqualTo(subject.getDisplayName());
    }

    @Test
    void toJson() {
        Contrast subject = generateRandomContrast(RNG.nextBoolean());

        ReadContext ctx = JsonPath.parse(subject.toJson().toString());

        assertThat(ctx.<String>read("$.id"))
                .isEqualTo(subject.getId());

        assertThat(ctx.<String>read("$.arrayDesignAccession"))
                .isEqualTo(subject.getArrayDesignAccession());

        assertThat(ctx.<String>read("$.displayName"))
                .isEqualTo(subject.getDisplayName());

        assertThat(ctx.<String>read("$.referenceAssayGroup.id"))
                .isEqualTo(subject.getReferenceAssayGroup().getId());
        assertThat(ctx.<List<String>>read("$.referenceAssayGroup.assayAccessions"))
                .containsAll(subject.getReferenceAssayGroup().getAssayIds());
        assertThat(ctx.<Integer>read("$.referenceAssayGroup.replicates"))
                .isEqualTo(subject.getReferenceAssayGroup().getAssays().size());

        assertThat(ctx.<String>read("$.testAssayGroup.id"))
                .isEqualTo(subject.getTestAssayGroup().getId());
        assertThat(ctx.<List<String>>read("$.testAssayGroup.assayAccessions"))
                .containsAll(subject.getTestAssayGroup().getAssayIds());
        assertThat(ctx.<Integer>read("$.testAssayGroup.replicates"))
                .isEqualTo(subject.getTestAssayGroup().getAssays().size());

        assertThat(ctx.<String>read("$.arrayDesignAccession"))
                .isEqualTo(subject.getArrayDesignAccession());
    }
}