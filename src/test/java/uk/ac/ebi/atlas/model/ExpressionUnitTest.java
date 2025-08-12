package uk.ac.ebi.atlas.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpressionUnitTest {

    @Test
    public void testRnaEnumToString() {
        // Test that the toString() method of the TPM enum returns "tpms"
        assertThat(ExpressionUnit.Absolute.Rna.TPM.toString()).isEqualTo("tpms");

        // Test that the toString() method of the FPKM enum returns "fpkm"
        assertThat(ExpressionUnit.Absolute.Rna.FPKM.toString()).isEqualTo("fpkm");
    }

    @Test
    public void testRnaEnumGetDatabaseValue() {
        // Test that the getDatabaseValue() method of the TPM enum returns "TPM"
        assertThat(ExpressionUnit.Absolute.Rna.TPM.getDatabaseValue()).isEqualTo("TPM");

        // Test that the getDatabaseValue() method of the FPKM enum returns "FPKM"
        assertThat(ExpressionUnit.Absolute.Rna.FPKM.getDatabaseValue()).isEqualTo("FPKM");
    }

    @Test
    public void testProteinEnumToString() {
        // Test that the toString() method of the PPB enum returns "parts per billion"
        assertThat(ExpressionUnit.Absolute.Protein.PPB.toString()).isEqualTo("parts per billion");

        // Test that the toString() method of the RA enum returns "relative abundance"
        assertThat(ExpressionUnit.Absolute.Protein.RA.toString()).isEqualTo("relative abundance");
    }

    @Test
    public void testProteinEnumGetDatabaseValue() {
        // Test that the getDatabaseValue() method of the PPB enum returns "PPB"
        assertThat(ExpressionUnit.Absolute.Protein.PPB.getDatabaseValue()).isEqualTo("PPB");

        // Test that the getDatabaseValue() method of the RA enum returns "RA"
        assertThat(ExpressionUnit.Absolute.Protein.RA.getDatabaseValue()).isEqualTo("RA");
    }

    @Test
    public void testRelativeEnumToString() {
        // Test that the toString() method of the FOLD_CHANGE enum returns "Log2 fold change"
        assertThat(ExpressionUnit.Relative.FOLD_CHANGE.toString()).isEqualTo("Log2 fold change");
    }

    @Test
    public void testRelativeEnumGetDatabaseValue() {
        // Test that the getDatabaseValue() method of the FOLD_CHANGE enum returns the same as toString()
        assertThat(ExpressionUnit.Relative.FOLD_CHANGE.getDatabaseValue()).isEqualTo(ExpressionUnit.Relative.FOLD_CHANGE.toString());
    }
}
