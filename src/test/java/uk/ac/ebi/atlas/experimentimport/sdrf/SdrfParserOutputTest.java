package uk.ac.ebi.atlas.experimentimport.sdrf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

class SdrfParserOutputTest {

    private SdrfParserOutput subject;

    @Test
    @DisplayName("gets a technology type list")
    void testGettersForTechnologyTypeList() {
        subject = new SdrfParserOutput(
                Optional.of(Arrays.asList("type1", "type2"))
        );

        assertThat(subject.getTechnologyType()).isEqualTo(Optional.of(Arrays.asList("type1", "type2")));
    }
}
