package uk.ac.ebi.atlas.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.Publication;

import javax.inject.Inject;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class EuropePmcClientIT {
    @Inject
    private EuropePmcClient subject;

    @Test
    public void publicationForValidDoi() {
        Optional<Publication> result = subject.getPublicationByDOI("10.1126/sciimmunol.aan8664");

        assertThat(result.isPresent()).isTrue();

        assertThat(result.orElseThrow(RuntimeException::new))
                .extracting("doi", "authors", "title")
                .isNotEmpty();

        assertThat(result.orElseThrow(RuntimeException::new).getDoi())
                .isEqualToIgnoringCase("10.1126/sciimmunol.aan8664");
    }

    @Test
    public void publicationForValidPubmedId() {
        Optional<Publication> result = subject.getPublicationByPubmedID("29352091");

        assertThat(result.isPresent()).isTrue();

        assertThat(result.orElseThrow(RuntimeException::new))
                .extracting("pubmedId", "authors", "title")
                .isNotEmpty();

        assertThat(result.orElseThrow(RuntimeException::new).getPubmedId()).isEqualToIgnoringCase("29352091");
    }

    @Test
    public void noResultForEmptyIdentifier() {
        Optional<Publication> result1 = subject.getPublicationByDOI("");
        Optional<Publication> result2 = subject.getPublicationByPubmedID("");

        assertThat(result1.isPresent()).isFalse();
        assertThat(result2.isPresent()).isFalse();
    }
}
