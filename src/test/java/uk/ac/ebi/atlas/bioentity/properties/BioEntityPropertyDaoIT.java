package uk.ac.ebi.atlas.bioentity.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.BioentityPropertyName;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BioEntityPropertyDaoIT {
    private static final String MOUSE_GENE_ID = "ENSMUSG00000029816";
    private static final int MIN_NUMBER_OF_PROPERTIES = 50;
    private static final int MIN_NUMBER_OF_ORTHOLOGS = 20;
    private static final int MIN_NUMBER_OF_GO_TERMS = 20;
    private static final int MIN_NUMBER_OF_DESIGN_ELEMENTS = 30;

    @Inject
    private BioEntityPropertyDao subject;

    @Test
    void fetchGenePageProperties() {
        Map<BioentityPropertyName, Set<String>> properties =
                subject.fetchGenePageProperties(MOUSE_GENE_ID);

        assertThat(numberOfValues(properties))
                .isGreaterThan(MIN_NUMBER_OF_PROPERTIES);
        assertThat(properties.get(BioentityPropertyName.SYNONYM))
                .hasSize(3);
        assertThat(properties.get(BioentityPropertyName.SYNONYM))
                .containsExactlyInAnyOrder("DC-HIL", "Dchil", "Osteoactivin");
        assertThat(properties.get(BioentityPropertyName.ORTHOLOG).size())
                .isGreaterThan(MIN_NUMBER_OF_ORTHOLOGS);
        assertThat(properties.get(BioentityPropertyName.GO).size())
                .isGreaterThan(MIN_NUMBER_OF_GO_TERMS);
        assertThat(properties.get(BioentityPropertyName.INTERPRO))
                .contains("IPR000601", "IPR022409", "IPR013783");
        assertThat(properties.get(BioentityPropertyName.ENSFAMILY_DESCRIPTION))
                .contains("PRECURSOR");
        assertThat(properties.get(BioentityPropertyName.ENSGENE))
                .contains("ENSMUSG00000029816");
        assertThat(properties.get(BioentityPropertyName.ENTREZGENE))
                .contains("93695");
        assertThat(properties.get(BioentityPropertyName.UNIPROT))
                .contains("A0A0N4SVG5", "Q8BVA0", "Q99P91");
        assertThat(properties.get(BioentityPropertyName.MGI_ID))
                .contains("MGI:1934765");
        assertThat(properties.get(BioentityPropertyName.GENE_BIOTYPE))
                .contains("protein_coding");
        assertThat(properties.get(BioentityPropertyName.DESIGN_ELEMENT).size())
                .isGreaterThan(MIN_NUMBER_OF_DESIGN_ELEMENTS);
    }

    @Test
    void findPropertyValuesForGeneId() {
        assertThat(subject.fetchPropertyValuesForGeneId("ENSG00000179218", BioentityPropertyName.SYMBOL))
                .contains("CALR");
        assertThat(subject.fetchPropertyValuesForGeneId("ENSMUSG00000029816", BioentityPropertyName.SYMBOL))
                .contains("Gpnmb");
    }

    @Test
    void validGeneIdReturnAssociatedSymbol() {
        String geneId = "ENSG00000001626";

        assertThat(subject.getSymbolForGeneId(geneId))
                .hasSize(1)
                .containsOnlyKeys(geneId);
    }

    @Test
    void geneIdWithoutSymbolReturnNoResults() {
        String geneId = "FAKE_GENE_ID";

        assertThat(subject.getSymbolForGeneId(geneId)).isEmpty();
    }


    @Test
    void validGeneIdsReturnAssociatedSymbols() {
        List<String> geneIds = Arrays.asList("ENSG00000001626", "ENSMUSG00000033952", "ENSDARG00000103754");

        assertThat(subject.getSymbolsForGeneIds(geneIds))
                .hasSize(3)
                .containsOnlyKeys(geneIds.toArray(new String[0]));
    }

    @Test
    void geneIdsWithoutSymbolsReturnNoResults() {
        List<String> geneIds = Collections.singletonList("FAKE_GENE_ID");

        assertThat(subject.getSymbolsForGeneIds(geneIds)).isEmpty();
    }

    @Test
    void emptyGeneIdListReturnsNoResults() {
        assertThat(subject.getSymbolsForGeneIds(emptyList())).isEmpty();
    }


    private int numberOfValues(Map<?, ? extends Set<?>> map) {
        int n = 0;
        for (Set<?> value : map.values()) {
            n += value.size();
        }
        return n;
    }

}
