package uk.ac.ebi.atlas.experimentimport.sdrf;

import org.junit.jupiter.api.Test;

import java.util.Arrays;


import static org.assertj.core.api.Assertions.assertThat;

class SdrfParserOutputTest {

    private SdrfParserOutput subject;

    @Test
    void testGetters() {
        subject = new SdrfParserOutput(
                Arrays.asList("type1", "type2")
        );

        assertThat(subject.getTechnologyType()).isEqualTo( Arrays.asList("type1", "type2"));
    }
}
