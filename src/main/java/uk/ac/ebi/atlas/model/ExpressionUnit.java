package uk.ac.ebi.atlas.model;

public interface ExpressionUnit {

    interface Absolute extends ExpressionUnit {
        enum Rna implements Absolute {
            FPKM,
            TPM
        }

        enum Protein implements Absolute {
            PPB("parts per billion"),
            RA("relative abundance");

            private final String unit;

            /**
             * @param unit
             */
            Protein(final String unit) {
                this.unit = unit;
            }

            /* (non-Javadoc)
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {
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
    }

}
