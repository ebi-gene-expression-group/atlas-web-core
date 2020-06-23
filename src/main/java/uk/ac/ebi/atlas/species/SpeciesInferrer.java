package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.bioentities.BioentitiesSearchDao;
import uk.ac.ebi.atlas.solr.analytics.AnalyticsSearchService;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SpeciesInferrer {
    private final BioentitiesSearchDao bioentitiesSearchDao;
    private final AnalyticsSearchService analyticsSearchService;
    private final SpeciesFactory speciesFactory;

    public SpeciesInferrer(BioentitiesSearchDao bioentitiesSearchDao,
                           AnalyticsSearchService analyticsSearchService,
                           SpeciesFactory speciesFactory) {
        this.bioentitiesSearchDao = bioentitiesSearchDao;
        this.analyticsSearchService = analyticsSearchService;
        this.speciesFactory = speciesFactory;
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

        var speciesCandidatesBuilder =
                ImmutableSet.<String>builder().addAll(analyticsSearchService.findSpecies(geneQuery, conditionQuery));

        if (conditionQuery.size() == 0 && speciesCandidatesBuilder.build().size() == 0) {
            speciesCandidatesBuilder.addAll(
                    bioentitiesSearchDao.searchSpecies(geneQuery).stream()
                            .map(speciesCandidate -> speciesFactory.create(speciesCandidate).getReferenceName())
                            .collect(toImmutableSet()));
        }

        var speciesCandidates = speciesCandidatesBuilder.build();

        return speciesCandidates.size() == 1 ?
                speciesFactory.create(speciesCandidates.iterator().next()) :
                speciesFactory.createUnknownSpecies();
    }
}
