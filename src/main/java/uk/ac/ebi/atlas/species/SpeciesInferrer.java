package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.bioentities.query.SolrQueryService;
import uk.ac.ebi.atlas.utils.EnsemblLookupClient;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SpeciesInferrer {
    private final SolrQueryService bioentitiesSearchService;
    private final SpeciesFactory speciesFactory;
    private final SpeciesFinder speciesFinder;
    private final EnsemblLookupClient ensemblLookupClient;

    public SpeciesInferrer(SolrQueryService bioentitiesSearchService,
                           SpeciesFactory speciesFactory,
                           SpeciesFinder speciesFinder,
                           EnsemblLookupClient ensemblLookupClient) {
        this.bioentitiesSearchService = bioentitiesSearchService;
        this.speciesFactory = speciesFactory;
        this.speciesFinder = speciesFinder;
        this.ensemblLookupClient = ensemblLookupClient;
    }

    public Species inferSpecies(@NotNull SemanticQuery geneQuery,
                                @NotNull SemanticQuery conditionQuery,
                                @NotNull String speciesString) {
        if (isBlank(speciesString)) {
            return inferSpecies(geneQuery, conditionQuery);
        }

        return speciesFactory.create(speciesString);
    }

    public Species inferSpeciesForGeneQuery(@NotNull SemanticQuery geneQuery) {
        return inferSpecies(geneQuery, SemanticQuery.create());
    }

    public Species inferSpeciesForGeneQuery(@NotNull SemanticQuery geneQuery, @NotNull String speciesString) {
        return inferSpecies(geneQuery, SemanticQuery.create(), speciesString);
    }

    private Species inferSpecies(SemanticQuery geneQuery, SemanticQuery conditionQuery) {
        if (geneQuery == null || geneQuery.isEmpty() && conditionQuery.isEmpty()) {
            return speciesFactory.createUnknownSpecies();
        }

        var speciesCandidatesBuilder = ImmutableSet.<String>builder();

        // Try Ensembl for ENS* IDs, then Solr bioentities, before analytics
        if (conditionQuery.size() == 0) {
            speciesCandidatesBuilder.addAll(
                    geneQuery.terms().stream()
                            .flatMap(this::inferSpeciesReferenceNamesForTerm)
                            .collect(ImmutableSet.toImmutableSet()));
        }

        if (speciesCandidatesBuilder.build().isEmpty()) {
            speciesCandidatesBuilder.addAll(speciesFinder.findSpecies(geneQuery, conditionQuery));
        }

        var speciesCandidates = speciesCandidatesBuilder.build();

        return speciesCandidates.size() == 1 ?
                speciesFactory.create(speciesCandidates.iterator().next()) :
                speciesFactory.createUnknownSpecies();
    }

    private Stream<String> inferSpeciesReferenceNamesForTerm(SemanticQueryTerm term) {
        if (EnsemblLookupClient.isEnsemblId(term.value())) {
            var fromEnsembl = ensemblLookupClient.lookupSpecies(term.value());
            if (fromEnsembl.isPresent()) {
                return Stream.of(fromEnsembl.get().getReferenceName());
            }
        }

        return bioentitiesSearchService.fetchSpecies(term).stream()
                .map(speciesFactory::create)
                .map(Species::getReferenceName);
    }
}
