package uk.ac.ebi.atlas.model;

public interface ExpressionUnit {
    /**
     * Returns the value to be used in database queries.
     * This is typically the enum name.
     */
    default String getDatabaseValue() {
        return toString();
    }

    interface Absolute extends ExpressionUnit {
        enum Rna implements Absolute {
            FPKM("fpkms"),
            fpkms("fpkms"),
            TPM("tpms"),
            tpms("tpms");

            private final String label;

            Rna(String label) {
                this.label = label;
            }

            public String getLabel() {
                return label;
            }

            @Override
            public String toString() {
                return label;
            }

            @Override
            public String getDatabaseValue() {
                return getLabel();
            }
        }

        enum Protein implements Absolute {
            PPB("ppb"), // parts per billion
            ppb("ppb"),
            RA("ra"), // relative abundance
            ra("ra");

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
                return unit;
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
            return name();
        }
    }
}
