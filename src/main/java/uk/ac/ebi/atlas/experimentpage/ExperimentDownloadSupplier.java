package uk.ac.ebi.atlas.experimentpage;

import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.atlas.experimentpage.context.BaselineRequestContext;
import uk.ac.ebi.atlas.experimentpage.context.BulkDifferentialRequestContext;
import uk.ac.ebi.atlas.experimentpage.context.DifferentialRequestContextFactory;
import uk.ac.ebi.atlas.experimentpage.context.MicroarrayRequestContext;
import uk.ac.ebi.atlas.experimentpage.differential.CanStreamSupplier;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExpression;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineProfile;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.profiles.ProfileStreamFilter;
import uk.ac.ebi.atlas.profiles.baseline.BaselineProfileStreamOptions;
import uk.ac.ebi.atlas.profiles.stream.MicroarrayProfileStreamFactory;
import uk.ac.ebi.atlas.profiles.stream.BulkDifferentialProfileStreamFactory;
import uk.ac.ebi.atlas.profiles.stream.ProfileStreamFactory;
import uk.ac.ebi.atlas.profiles.stream.ProteomicsBaselineProfileStreamFactory;
import uk.ac.ebi.atlas.profiles.stream.RnaSeqBaselineProfileStreamFactory;
import uk.ac.ebi.atlas.profiles.writer.BaselineProfilesWriterFactory;
import uk.ac.ebi.atlas.profiles.writer.BulkDifferentialProfilesWriterFactory;
import uk.ac.ebi.atlas.profiles.writer.MicroarrayProfilesWriterFactory;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.solr.bioentities.query.GeneQueryResponse;
import uk.ac.ebi.atlas.solr.bioentities.query.SolrQueryService;
import uk.ac.ebi.atlas.web.BaselineRequestPreferences;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;
import uk.ac.ebi.atlas.web.ExperimentPageRequestPreferences;
import uk.ac.ebi.atlas.web.MicroarrayRequestPreferences;
import uk.ac.ebi.atlas.web.ProteomicsBaselineRequestPreferences;
import uk.ac.ebi.atlas.web.RnaSeqBaselineRequestPreferences;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ExperimentDownloadSupplier<E extends Experiment, P extends ExperimentPageRequestPreferences>
                      extends CanStreamSupplier<E> {

    @Override
    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.DATA;
    }

    protected abstract void write(HttpServletResponse response,
                                  P preferences, E experiment,
                                  String id) throws IOException;

    public abstract static class ExperimentDownloadFileSupplier<
                                         E extends Experiment, P extends ExperimentPageRequestPreferences>
                                 extends ExperimentDownloadSupplier<E, P> {

        protected void write(HttpServletResponse response,
                             P preferences,
                             E experiment,
                             final String id) throws IOException {
            response.setHeader(
                    "Content-Disposition",
                    MessageFormat.format(
                            "attachment; filename=\"{0}-query-results.{1}\"", experiment.getAccession(), id));
            response.setContentType("text/plain; charset=utf-8");
            write(response.getWriter(), preferences, experiment);
        }

        protected abstract void write(Writer writer, P preferences, E experiment);

        protected ExternallyAvailableContent getOne(final E experiment,
                                                    final P preferences,
                                                    final String id,
                                                    String description) {
            return new ExternallyAvailableContent(makeUri(id),
                    ExternallyAvailableContent.Description.create("icon-tsv", description), response -> {
                try {
                    response.setHeader(
                            "Content-Disposition",
                            MessageFormat.format("attachment; filename=\"{0}-query-results.{1}\"",
                                    experiment.getAccession(),
                                    id));
                    response.setContentType("text/plain; charset=utf-8");

                    write(response.getWriter(), preferences, experiment);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
    }

    public abstract static class Baseline<U extends ExpressionUnit.Absolute, P extends BaselineRequestPreferences<U>>
                                 extends ExperimentDownloadFileSupplier<BaselineExperiment, P> {

        private final BaselineProfilesWriterFactory<U> baselineProfilesWriterFactory;
        private final SolrQueryService solrQueryService;
        private final ProfileStreamFactory<
                AssayGroup, BaselineExpression, BaselineExperiment, BaselineProfileStreamOptions<U>, BaselineProfile>
                baselineProfileStreamFactory;

        protected Baseline(BaselineProfilesWriterFactory<U> baselineProfilesWriterFactory,
                           SolrQueryService solrQueryService,
                           ProfileStreamFactory<
                                   AssayGroup,
                                   BaselineExpression,
                                   BaselineExperiment,
                                   BaselineProfileStreamOptions<U>,
                                   BaselineProfile> baselineProfileStreamFactory) {
            this.baselineProfilesWriterFactory = baselineProfilesWriterFactory;
            this.solrQueryService = solrQueryService;
            this.baselineProfileStreamFactory = baselineProfileStreamFactory;
        }

        protected void write(Writer writer, P preferences, BaselineExperiment experiment) {

            BaselineRequestContext<U> requestContext = new BaselineRequestContext<>(preferences, experiment);
            GeneQueryResponse geneQueryResponse =
                    solrQueryService.fetchResponse(requestContext.getGeneQuery(), requestContext.getSpecies());
            baselineProfileStreamFactory.write(experiment, requestContext,
                    geneQueryResponse.getAllGeneIds(), ProfileStreamFilter.create(requestContext),
                    baselineProfilesWriterFactory.create(writer, requestContext));
        }
    }

    @Named
    public static class Proteomics
                        extends Baseline<
                                    ExpressionUnit.Absolute.Protein,
                                    BaselineRequestPreferences<ExpressionUnit.Absolute.Protein>> {

        @Inject
        public Proteomics(BaselineProfilesWriterFactory<ExpressionUnit.Absolute.Protein> baselineProfilesWriterFactory,
                          SolrQueryService solrQueryService,
                          ProteomicsBaselineProfileStreamFactory baselineProfileStreamFactory) {
            super(baselineProfilesWriterFactory, solrQueryService, baselineProfileStreamFactory);
        }

        @Override
        public Collection<ExternallyAvailableContent> get(final BaselineExperiment experiment) {
            return Collections.singleton(
                    getOne(
                            experiment,
                            ProteomicsBaselineRequestPreferences.requestAllData(),
                            "tsv",
                            "Expression values across all genes"));
        }
    }

    @Named
    public static class RnaSeqBaseline
                        extends Baseline<ExpressionUnit.Absolute.Rna, RnaSeqBaselineRequestPreferences> {
        private final DataFileHub dataFileHub;

        @Inject
        public RnaSeqBaseline(BaselineProfilesWriterFactory<ExpressionUnit.Absolute.Rna> baselineProfilesWriterFactory,
                              SolrQueryService solrQueryService,
                              RnaSeqBaselineProfileStreamFactory baselineProfileStreamFactory,
                              DataFileHub dataFileHub) {
            super(baselineProfilesWriterFactory, solrQueryService, baselineProfileStreamFactory);
            this.dataFileHub = dataFileHub;
        }

        @Override
        public Collection<ExternallyAvailableContent> get(final BaselineExperiment experiment) {
            return dataFileHub.getRnaSeqBaselineExperimentFiles(experiment.getAccession()).dataFiles().stream()
                    .map(
                            unit ->
                                    getOne(
                                            experiment,
                                            RnaSeqBaselineRequestPreferences.requestAllData(unit),
                                            MessageFormat.format("{0}s.tsv", unit.toString().toLowerCase()),
                                            MessageFormat.format("Expression values across all genes ({0})", unit)))
                    .collect(Collectors.toList());
        }
    }

    @Named
    public static class Microarray
                        extends ExperimentDownloadSupplier<MicroarrayExperiment, MicroarrayRequestPreferences> {

        private final SolrQueryService solrQueryService;
        private final MicroarrayProfileStreamFactory microarrayProfileStreamFactory;
        private final MicroarrayProfilesWriterFactory microarrayProfilesWriterFactory;

        @Inject
        public Microarray(SolrQueryService solrQueryService,
                          MicroarrayProfileStreamFactory microarrayProfileStreamFactory,
                          MicroarrayProfilesWriterFactory microarrayProfilesWriterFactory) {
            this.solrQueryService = solrQueryService;
            this.microarrayProfileStreamFactory = microarrayProfileStreamFactory;
            this.microarrayProfilesWriterFactory = microarrayProfilesWriterFactory;
        }

        private Function<Writer, Void> fetchAndWriteGeneProfiles(final MicroarrayExperiment experiment,
                                                                 final MicroarrayRequestPreferences preferences,
                                                                 final GeneQueryResponse geneQueryResponse) {
            final MicroarrayRequestContext context =
                    new DifferentialRequestContextFactory.Microarray().create(experiment, preferences);

            return writer -> {
                microarrayProfileStreamFactory.write(
                        experiment,
                        context,
                        geneQueryResponse.getAllGeneIds(), ProfileStreamFilter.create(context),
                        microarrayProfilesWriterFactory.create(writer, context));
                return null;
            };
        }

        private java.util.function.Function<HttpServletResponse, Void> stream(
                MicroarrayExperiment experiment, MicroarrayRequestPreferences preferences) {

            GeneQueryResponse geneQueryResponse =
                    solrQueryService.fetchResponse(preferences.getGeneQuery(), experiment.getSpecies());

            List<Pair<String, Function<Writer, Void>>> documents = new ArrayList<>();
            for (String arrayDesign : experiment.getArrayDesignAccessions()) {
                documents.add(
                        Pair.of(
                                MessageFormat.format(
                                        "{0}-{1}-query-results.tsv", experiment.getAccession(), arrayDesign),
                                fetchAndWriteGeneProfiles(experiment, preferences, geneQueryResponse)));
            }
            return documents.size() == 1 ?
                    streamFile(documents.get(0)) :
                    streamFolder(experiment.getAccession() + "-query-results", documents);

        }

        @Override
        protected void write(HttpServletResponse response,
                             MicroarrayRequestPreferences preferences,
                             MicroarrayExperiment experiment,
                             String id) {
            stream(experiment, preferences).apply(response);
        }

        @Override
        public Collection<ExternallyAvailableContent> get(MicroarrayExperiment experiment) {
            MicroarrayRequestPreferences preferences = new MicroarrayRequestPreferences();
            preferences.setFoldChangeCutoff(0.0);
            preferences.setCutoff(1.0);
            return Collections.singleton(
                    new ExternallyAvailableContent(
                            makeUri("query-results"),
                            ExternallyAvailableContent.Description.create(
                                    "icon-tsv", "All expression results in the experiment"),
                            stream(experiment, preferences)));
        }
    }

    @Named
    public static class BulkDifferential
                        extends ExperimentDownloadFileSupplier<
                                DifferentialExperiment, DifferentialRequestPreferences> {

        private final BulkDifferentialProfileStreamFactory bulkDifferentialProfileStreamFactory;
        private final SolrQueryService solrQueryService;
        private final BulkDifferentialProfilesWriterFactory bulkDifferentialProfilesWriterFactory;

        @Inject
        public BulkDifferential(BulkDifferentialProfileStreamFactory bulkDifferentialProfileStreamFactory,
                                  SolrQueryService solrQueryService,
                                               BulkDifferentialProfilesWriterFactory bulkDifferentialProfilesWriterFactory) {
            this.bulkDifferentialProfileStreamFactory = bulkDifferentialProfileStreamFactory;
            this.solrQueryService = solrQueryService;
            this.bulkDifferentialProfilesWriterFactory = bulkDifferentialProfilesWriterFactory;
        }

        @Override
        protected void write(Writer responseWriter,
                             DifferentialRequestPreferences differentialRequestPreferences,
                             DifferentialExperiment experiment) {
            BulkDifferentialRequestContext context =
                    new DifferentialRequestContextFactory.RnaSeq().create(experiment, differentialRequestPreferences);
            GeneQueryResponse geneQueryResponse =
                    solrQueryService.fetchResponse(context.getGeneQuery(), experiment.getSpecies());
            bulkDifferentialProfileStreamFactory.write(
                    experiment,
                    context,
                    geneQueryResponse.getAllGeneIds(),
                    ProfileStreamFilter.create(context),
                    bulkDifferentialProfilesWriterFactory.create(responseWriter, context));
        }

        @Override
        public Collection<ExternallyAvailableContent> get(DifferentialExperiment experiment) {
            DifferentialRequestPreferences preferences = new DifferentialRequestPreferences();
            if (experiment.getType().isProteomicsDifferential()) {
                preferences.setFoldChangeCutoff(0.0);
                preferences.setCutoff(0.5);
            } else {
                preferences.setFoldChangeCutoff(0.0);
                preferences.setCutoff(1.0);
            }

            return Collections.singleton(
                    getOne(experiment, preferences, "tsv", "All expression results in the experiment"));
        }
    }

}
