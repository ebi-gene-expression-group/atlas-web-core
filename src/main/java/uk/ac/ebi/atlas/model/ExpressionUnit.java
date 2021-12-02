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

            private final String text;

            /**
             * @param text
             */
            Protein(final String text) {
                this.text = text;
            }

            /* (non-Javadoc)
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {
                return text;
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
