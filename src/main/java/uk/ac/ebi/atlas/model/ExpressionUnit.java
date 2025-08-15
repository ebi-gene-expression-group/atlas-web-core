package uk.ac.ebi.atlas.model;

public interface ExpressionUnit {

    String getDatabaseValue();

    interface Absolute extends ExpressionUnit {
        enum Rna implements Absolute {
            FPKM("fpkms"),
            TPM("tpms");

            private final String label;

            Rna(final String label) {
                this.label = label;
            }

            public String getLabel() {
                return label;
            }

            public String getDatabaseValue() {
                return label;
            }
        }

        enum Protein implements Absolute {
            PPB("parts per billion"),
            RA("relative abundance");

            private final String unit;

            Protein(final String unit) {
                this.unit = unit;
            }

            @Override
            public String toString() {
                return unit;
            }

            @Override
            public String getDatabaseValue() {
                return toString();
            }
        }
    }

    enum Relative implements ExpressionUnit {
        FOLD_CHANGE;

        @Override
        public String toString() {
            return "Log2 fold change";
        }

        @Override
        public String getDatabaseValue() {
            return toString();
        }
    }
}
