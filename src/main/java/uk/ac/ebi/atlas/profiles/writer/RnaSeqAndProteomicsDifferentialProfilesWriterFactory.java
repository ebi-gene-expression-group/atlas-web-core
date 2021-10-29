package uk.ac.ebi.atlas.profiles.writer;

import uk.ac.ebi.atlas.experimentpage.context.RnaSeqRequestContext;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.RnaSeqProfile;

import javax.inject.Named;

@Named
public class RnaSeqAndProteomicsDifferentialProfilesWriterFactory extends
        DifferentialProfilesWriterFactory<DifferentialExpression, RnaSeqProfile, RnaSeqRequestContext> {
}
