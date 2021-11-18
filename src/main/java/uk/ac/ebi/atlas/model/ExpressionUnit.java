package uk.ac.ebi.atlas.model;

public interface ExpressionUnit {

    interface Absolute extends ExpressionUnit {
        enum Rna implements Absolute {
            FPKM,
            TPM
        }

        enum Protein implements Absolute {
            PPB,
            RA;

            @Override
            public String toString() {
                return "parts per billion";
            }
        }

//        enum ProteinDiaSwath implements Absolute {
//
//
//            @Override
//            public String toString() {
//                return "relative abundance";
//            }
//        }

     }

    enum Relative implements ExpressionUnit {
        FOLD_CHANGE;

        @Override
        public String toString() {
            return "Log2 fold change";
        }
    }

}
