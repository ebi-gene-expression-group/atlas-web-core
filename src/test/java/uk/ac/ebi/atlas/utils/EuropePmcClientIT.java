package uk.ac.ebi.atlas.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class EuropePmcClientIT {
    @Inject
    private EuropePmcClient subject;

    @Test
    void publicationForValidDoi() {
        var result = subject.getPublicationByDoi("10.1126/sciimmunol.aan8664");

        assertThat(result.isPresent()).isTrue();

        assertThat(result.orElseThrow(RuntimeException::new))
                .extracting("doi", "authors", "title")
                .isNotEmpty();

        assertThat(result.orElseThrow(RuntimeException::new).getDoi())
                .isEqualToIgnoringCase("10.1126/sciimmunol.aan8664");
    }

    @Test
    void publicationForValidPubmedId() {
        var result = subject.getPublicationByPubmedId("29352091");

        assertThat(result.isPresent()).isTrue();

        assertThat(result.orElseThrow(RuntimeException::new))
                .extracting("pubmedId", "authors", "title")
                .isNotEmpty();

        assertThat(result.orElseThrow(RuntimeException::new).getPubmedId()).isEqualToIgnoringCase("29352091");
    }

    @Test
    void noResultForEmptyIdentifier() {
        var result1 = subject.getPublicationByDoi("");
        var result2 = subject.getPublicationByPubmedId("");

        assertThat(result1.isPresent()).isFalse();
        assertThat(result2.isPresent()).isFalse();
    }
}
