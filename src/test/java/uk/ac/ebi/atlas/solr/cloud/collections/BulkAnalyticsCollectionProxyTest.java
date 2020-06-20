package uk.ac.ebi.atlas.solr.cloud.collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import uk.ac.ebi.atlas.model.ExpressionUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.LOG_2_FOLD_CHANGE;

class BulkAnalyticsCollectionProxyTest {
    @Test
    void tpmAndFpkmFieldsAreDifferent() {
        assertThat(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.TPM))
                .isNotEqualTo(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.FPKM));
    }

    @Test
    void proteomicUnitsAndTpmsAreTheSameField() {
        assertThat(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Protein.ANY))
                .isEqualTo(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.TPM));
    }

    @Test
    void rnaSeqBaselineExperimentsHaveQuartilesField() {
        var tpmFields = BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.TPM);
        assertThat(tpmFields.getLeft())
                .isNotEqualTo(tpmFields.getRight());

        var fpkmFields = BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.FPKM);
        assertThat(fpkmFields.getLeft())
                .isNotEqualTo(fpkmFields.getRight());

    }

    @Test
    void differentialExpressionHasNoQuartilesField() {
        var result = BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Relative.FOLD_CHANGE);
        assertThat(result.getLeft())
                .isEqualTo(result.getRight())
                .isEqualTo(LOG_2_FOLD_CHANGE);
    }
}
