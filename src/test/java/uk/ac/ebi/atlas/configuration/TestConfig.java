package uk.ac.ebi.atlas.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.bioentity.properties.ExpressedBioentityFinder;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.species.SpeciesFinder;
import uk.ac.ebi.atlas.trader.ExperimentRepository;
import uk.ac.ebi.atlas.utils.BioentityIdentifiersReader;

import java.util.HashSet;

@Configuration
// Enabling component scanning will also load BasePathsConfig, JdbcConfig and SolrConfig, so just using this class as
// application context is enough in integration tests
@ComponentScan(basePackages = "uk.ac.ebi.atlas",
               includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = TestJdbcConfig.class),
               excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = JdbcConfig.class))
public class TestConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ExperimentRepository experimentRepository() {
        return experimentAccession -> null;
    }

    @Bean
    public BioentityIdentifiersReader bioentityIdentifiersReader() {
        return new BioentityIdentifiersReader() {
            @Override
            protected int addBioentityIdentifiers(@NotNull HashSet<String> bioentityIdentifiers,
                                                  @NotNull ExperimentType experimentType) {
                return 0;
            }

            @Override
            public HashSet<String> getBioentityIdsFromExperiment(@NotNull String experimentAccession) {
                return new HashSet<>();
            }
        };
    }

    @Bean
    public ExpressedBioentityFinder expressedBioentityFinder() {
        return bioentityIdentifier -> true;
    }

    @Bean
    public SpeciesFinder speciesFinder() {
        return new SpeciesFinder() {};
    }
}
