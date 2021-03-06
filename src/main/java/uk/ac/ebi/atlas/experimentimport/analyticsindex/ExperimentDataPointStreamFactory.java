package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import uk.ac.ebi.atlas.experimentimport.analytics.baseline.BaselineAnalyticsInputStreamFactory;
import uk.ac.ebi.atlas.experimentimport.analytics.differential.microarray.MicroarrayDifferentialAnalytics;
import uk.ac.ebi.atlas.experimentimport.analytics.differential.microarray.MicroarrayDifferentialAnalyticsInputStreamFactory;
import uk.ac.ebi.atlas.experimentimport.analytics.differential.rnaseq.RnaSeqDifferentialAnalyticsInputStreamFactory;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.conditions.ConditionsLookupService;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.BaselineExperimentDataPoint;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.BaselineExperimentDataPointStream;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.DifferentialExperimentDataPoint;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.DifferentialExperimentDataPointStream;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.ExperimentDataPoint;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.MicroarrayExperimentDataPoint;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.MicroarrayExperimentDataPointStream;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Component
public class ExperimentDataPointStreamFactory {
    private final ConditionsLookupService conditionsLookupService;
    private final MicroarrayDifferentialAnalyticsInputStreamFactory microarrayDifferentialAnalyticsInputStreamFactory;
    private final RnaSeqDifferentialAnalyticsInputStreamFactory rnaSeqDifferentialAnalyticsInputStreamFactory;
    private final BaselineAnalyticsInputStreamFactory baselineAnalyticsInputStreamFactory;

    public ExperimentDataPointStreamFactory(ConditionsLookupService conditionsLookupService,
                                            MicroarrayDifferentialAnalyticsInputStreamFactory
                                                         microarrayDifferentialAnalyticsInputStreamFactory,
                                            RnaSeqDifferentialAnalyticsInputStreamFactory
                                                        rnaSeqDifferentialAnalyticsInputStreamFactory,
                                            BaselineAnalyticsInputStreamFactory
                                                        baselineAnalyticsInputStreamFactory) {

        this.conditionsLookupService = conditionsLookupService;
        this.microarrayDifferentialAnalyticsInputStreamFactory = microarrayDifferentialAnalyticsInputStreamFactory;
        this.rnaSeqDifferentialAnalyticsInputStreamFactory = rnaSeqDifferentialAnalyticsInputStreamFactory;
        this.baselineAnalyticsInputStreamFactory = baselineAnalyticsInputStreamFactory;

    }

    public ObjectInputStream<? extends ExperimentDataPoint> stream(Experiment experiment) throws IOException {
        if (experiment instanceof MicroarrayExperiment) {
            return stream((MicroarrayExperiment) experiment);
        } else if (experiment instanceof DifferentialExperiment) {
            return stream((DifferentialExperiment) experiment);
        } else if (experiment instanceof BaselineExperiment) {
            return stream((BaselineExperiment) experiment);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported experiment type: %s", experiment.getType()));
        }
    }

    private ObjectInputStream<BaselineExperimentDataPoint> stream(BaselineExperiment experiment) throws IOException {
        return new BaselineExperimentDataPointStream(
                experiment,
                baselineAnalyticsInputStreamFactory.create(experiment.getAccession(), experiment.getType()),
                conditionsLookupService.conditionsPerDataColumnDescriptor(experiment));
    }

    private ObjectInputStream<DifferentialExperimentDataPoint> stream(DifferentialExperiment experiment)
            throws IOException {
        return new DifferentialExperimentDataPointStream(
                experiment,
                rnaSeqDifferentialAnalyticsInputStreamFactory.create(experiment.getAccession()),
                conditionsLookupService.conditionsPerDataColumnDescriptor(experiment),
                buildNumReplicatesByContrastId(experiment));
    }

    private ObjectInputStream<MicroarrayExperimentDataPoint> stream(MicroarrayExperiment experiment)
            throws IOException {
        Iterator<String> it = experiment.getArrayDesignAccessions().iterator();
        ImmutableList.Builder<ObjectInputStream<? extends MicroarrayDifferentialAnalytics>> builder =
                ImmutableList.builder();
        while (it.hasNext()) {
            builder.add(
                    microarrayDifferentialAnalyticsInputStreamFactory.create(experiment.getAccession(), it.next()));
        }

        return new MicroarrayExperimentDataPointStream(
                experiment,
                builder.build(),
                conditionsLookupService.conditionsPerDataColumnDescriptor(experiment),
                buildNumReplicatesByContrastId(experiment));
    }

    private Map<String, Integer> buildNumReplicatesByContrastId(DifferentialExperiment experiment) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();

        for (Contrast contrast : experiment.getDataColumnDescriptors()) {
            int numReplicates =
                    Math.min(
                            contrast.getReferenceAssayGroup().getAssays().size(),
                            contrast.getTestAssayGroup().getAssays().size());
            builder.put(contrast.getId(), numReplicates);
        }

        return builder.build();
    }
}
