package uk.ac.ebi.atlas.search.bioentities;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.species.SpeciesFactory;

import javax.inject.Inject;

import java.io.UncheckedIOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER_DV;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME_DV;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE_DV;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES_DV;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BioentitiesSearchDaoIT {
    private static final Random RNG = ThreadLocalRandom.current();

    private static final ImmutableList<SchemaField<BioentitiesCollectionProxy>> BIOENTITIES_FIELDS =
            ImmutableList.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE);
    private static final ImmutableList<SchemaField<BioentitiesCollectionProxy>> BIOENTITIES_FIELDS_DV =
            ImmutableList.of(BIOENTITY_IDENTIFIER_DV, SPECIES_DV, PROPERTY_NAME_DV, PROPERTY_VALUE_DV);

    @Inject
    private SpeciesFactory speciesFactory;

    @Inject
    private BioentitiesSearchDao subject;

    @Test
    void throwIfFieldDoesNotHaveDocValues() {
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() ->
                    subject.parseStringFieldFromMatchingDocs(
                            SemanticQuery.create(SemanticQueryTerm.create("foo")),
                            BIOENTITIES_FIELDS.get(RNG.nextInt(BIOENTITIES_FIELDS.size()))));
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() ->
                        subject.parseStringFieldFromMatchingDocs(
                                SemanticQuery.create(SemanticQueryTerm.create("foo")),
                                generateRandomSpecies(),
                                BIOENTITIES_FIELDS.get(RNG.nextInt(BIOENTITIES_FIELDS.size()))));
    }

    @Test
    void emptyQueryReturnsNoDocuments() {
        assertThat(
                subject.parseStringFieldFromMatchingDocs(
                        SemanticQuery.create(),
                        BIOENTITIES_FIELDS_DV.get(RNG.nextInt(BIOENTITIES_FIELDS_DV.size()))))
        .isEmpty();
    }

    @Test
    void emptyQueryTermReturnsNoDocuments() {
        assertThat(
                subject.parseStringFieldFromMatchingDocs(
                        SemanticQuery.create(SemanticQueryTerm.create(generateBlankString())),
                        BIOENTITIES_FIELDS_DV.get(RNG.nextInt(BIOENTITIES_FIELDS_DV.size()))))
                .isEmpty();

        var emptyQueryTerms = IntStream.range(1, RNG.nextInt(100)).boxed()
                .map(__ ->
                        RNG.nextBoolean() ?
                                SemanticQueryTerm.create(generateBlankString()) :
                                SemanticQueryTerm.create(generateBlankString(), generateBlankString()))
                .collect(toImmutableSet());
        assertThat(
                subject.parseStringFieldFromMatchingDocs(
                        SemanticQuery.create(emptyQueryTerms),
                        BIOENTITIES_FIELDS_DV.get(RNG.nextInt(BIOENTITIES_FIELDS_DV.size()))))
                .isEmpty();
    }

    @Test
    void nonExistingSpeciesReturnsNoDocuments() {
        assertThat(
                subject.parseStringFieldFromMatchingDocs(
                        SemanticQuery.create(SemanticQueryTerm.create("of")),
                        generateRandomSpecies(),
                        BIOENTITIES_FIELDS_DV.get(RNG.nextInt(BIOENTITIES_FIELDS_DV.size()))))
                .isEmpty();
    }
    
    @Test
    void canFilterBySpecies() {
        var searchText = "expression";
        var allSpeciesResultCount =
                subject.parseStringFieldFromMatchingDocs(
                        SemanticQuery.create(SemanticQueryTerm.create(searchText)),
                        BIOENTITY_IDENTIFIER_DV).size();

        assertThat(
                subject.parseStringFieldFromMatchingDocs(
                        SemanticQuery.create(SemanticQueryTerm.create(searchText)),
                        speciesFactory.create("Homo sapiens"),
                        BIOENTITY_IDENTIFIER_DV))
                .isNotEmpty()
                .allMatch(str -> str.startsWith("ENSG"))
                .size().isLessThan(allSpeciesResultCount);
    }
}
