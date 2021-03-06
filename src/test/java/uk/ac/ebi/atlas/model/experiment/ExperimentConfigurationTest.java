package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.ac.ebi.atlas.commons.readers.XmlReader;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ExperimentConfigurationTest {
    private static Path tmpFilePath;

    private static final String MICROARRAY_CONFIGURATION_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<configuration experimentType=\"microarray_1colour_mrna_differential\" r_data=\"1\">\n" +
                    "    <analytics>\n" +
                    "        <array_design>\n" +
                    "            A-MEXP-1551\n" +
                    "        </array_design>\n" +
                    "        <assay_groups>\n" +
                    "            <assay_group id=\"g1\">\n" +
                    "                <assay>A</assay>\n" +
                    "            </assay_group>\n" +
                    "            <assay_group id=\"g2\">\n" +
                    "                <assay technical_replicate_id=\"t1\">B</assay>\n" +
                    "                <assay technical_replicate_id=\"t1\">C</assay>\n" +
                    "            </assay_group>\n" +
                    "            <assay_group id=\"g3\">\n" +
                    "                <assay>D</assay>\n" +
                    "                <assay>E</assay>\n" +
                    "            </assay_group>\n" +
                    "            <assay_group id=\"g4\">\n" +
                    "                <assay>A</assay>\n" +
                    "                <assay technical_replicate_id=\"t2\">F</assay>\n" +
                    "                <assay technical_replicate_id=\"t2\">G</assay>\n" +
                    "                <assay>D</assay>\n" +
                    "                <assay technical_replicate_id=\"t2\">H</assay>\n" +
                    "            </assay_group>\n" +
                    "            <assay_group id=\"g5\">\n" +
                    "                <assay>A</assay>\n" +
                    "                <assay technical_replicate_id=\"t3\">I</assay>\n" +
                    "                <assay technical_replicate_id=\"t3\">J</assay>\n" +
                    "                <assay>D</assay>\n" +
                    "                <assay technical_replicate_id=\"t3\">K</assay>\n" +
                    "                <assay>F</assay>\n" +
                    "                <assay technical_replicate_id=\"t4\">L</assay>\n" +
                    "                <assay technical_replicate_id=\"t4\">M</assay>\n" +
                    "            </assay_group>\n" +
                    "        </assay_groups>\n" +
                    "        <contrasts>\n" +
                    "            <contrast id=\"g1_g2\" >\n" +
                    "                <name>'g1' vs 'g2'</name>\n" +
                    "                <reference_assay_group>g1</reference_assay_group>\n" +
                    "                <test_assay_group>g2</test_assay_group>\n" +
                    "            </contrast>\n" +
                    "            <contrast id=\"g1_g3\" cttv_primary=\"1\">\n" +
                    "                <name>'g1' vs 'g3'</name>\n" +
                    "                <reference_assay_group>g1</reference_assay_group>\n" +
                    "                <test_assay_group>g3</test_assay_group>\n" +
                    "            </contrast>\n" +
                    "        </contrasts>\n" +
                    "    </analytics>\n" +
                    "</configuration>\n";

    @BeforeClass
    public static void setUpClass() throws Exception {
        // In Commons Configuration 2, XMLConfiguration needs at least a well-formed XML file:
        // http://stackoverflow.com/questions/39573880/apache-commons-configuration2-how-to-read-data-from-inputstream
        tmpFilePath = Files.createTempFile("dummy", ".xml");
        Files.write(tmpFilePath, ImmutableList.of("<_/>"), Charset.forName("UTF-8"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Files.delete(tmpFilePath);
    }

    private ExperimentConfiguration testConfiguration(String xml) {
        final class XmlReaderMock extends XmlReader {
            private Document document;
            private XmlReaderMock(XMLConfiguration xmlConfiguration) {
                super(xmlConfiguration);
            }

            public void setDocument(Document document) {
                this.document = document;
            }

            @Override
            public Document getDocument() {
                return document;
            }
        }
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
                new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                        .configure(params.xml()
                                .setPath(tmpFilePath.toString())
                                .setExpressionEngine(new XPathExpressionEngine()));

        XMLConfiguration xmlConfiguration;
        Document document;
        try {
            xmlConfiguration = fileBuilder.getConfiguration();
            xmlConfiguration.read(inputStream);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream.reset();
            document = builder.parse(inputStream);
        } catch (ConfigurationException | IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }


        XmlReaderMock xmlReaderMock = new XmlReaderMock(xmlConfiguration);
        xmlReaderMock.setDocument(document);

        return new ExperimentConfiguration(xmlReaderMock);
    }

    @Test
    public void testGetAssayGroups()  {
        List<AssayGroup> assayGroups = testConfiguration(MICROARRAY_CONFIGURATION_XML).getAssayGroups();
        assertThat(assayGroups, hasSize(5));
    }

    @Test
    public void replicatesIsSumOfUniqueTechnicalReplicatesAndUnqualifiedAssays() {
        List<AssayGroup> assayGroups = testConfiguration(MICROARRAY_CONFIGURATION_XML).getAssayGroups();
        assertThat(assayGroups.get(0).getId(), is("g1"));
        assertThat(assayGroups.get(1).getId(), is("g2"));
        assertThat(assayGroups.get(2).getId(), is("g3"));
        assertThat(assayGroups.get(3).getId(), is("g4"));
        assertThat(assayGroups.get(4).getId(), is("g5"));

        assertThat(assayGroups.get(0).getAssays(), hasSize(1));
        assertThat(assayGroups.get(1).getAssays(), hasSize(1));
        assertThat(assayGroups.get(2).getAssays(), hasSize(2));
        assertThat(assayGroups.get(3).getAssays(), hasSize(3));
        assertThat(assayGroups.get(4).getAssays(), hasSize(5));
    }

    @Test
    public void testGetContrasts()  {
        List<Contrast> contrasts = testConfiguration(MICROARRAY_CONFIGURATION_XML).getContrasts();
        assertThat(contrasts, hasSize(2));
        Contrast contrast = contrasts.get(0);
        assertThat(contrast.getId(), is("g1_g2"));
        assertThat(contrast.getDisplayName(), is("'g1' vs 'g2'"));
        assertThat(contrast.getReferenceAssayGroup().getAssayIds(), contains("A"));
        assertThat(contrast.getTestAssayGroup().getAssayIds(), contains("B", "C"));
        Contrast otherContrast = contrasts.get(1);
        assertThat(otherContrast.getId(), is(not(contrast.getId())));
    }

    @Test
    public void testGetExperimentType() {
        assertThat(
                testConfiguration(MICROARRAY_CONFIGURATION_XML).getExperimentType(),
                is(ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL));
        assertThat(testConfiguration(
                "<configuration experimentType=\"rnaseq_mrna_baseline\"></configuration>").getExperimentType(),
                is(ExperimentType.RNASEQ_MRNA_BASELINE));
    }

    @Test
    public void testOpenTargetsContrasts() {
        assertThat(
                testConfiguration(MICROARRAY_CONFIGURATION_XML).getContrastAndAnnotationPairs().stream()
                    .filter(Pair::getRight)
                    .collect(toList()),
                hasSize(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContrastNeedsDisplayName() {
        testConfiguration(
                "<configuration experimentType=\"microarray_1colour_mrna_differential\">" +
                        "<analytics>" +
                        "<contrasts>" +
                        "<contrast id=\"id\"></contrast>" +
                        "</contrasts>" +
                        "</analytics>" +
                        "</configuration>"
        ).getContrasts();
    }

    @Test
    public void testGetArrayDesignNames() {
        assertThat(testConfiguration(MICROARRAY_CONFIGURATION_XML).getArrayDesignAccessions(), hasItem("A-MEXP-1551"));
    }
}
