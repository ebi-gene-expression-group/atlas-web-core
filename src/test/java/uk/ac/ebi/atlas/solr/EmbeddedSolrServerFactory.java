package uk.ac.ebi.atlas.solr;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

@Component
public class EmbeddedSolrServerFactory {
    private final CoreContainer coreContainer;

    public EmbeddedSolrServerFactory() throws IOException {
        var solrTempDirectory = Files.createTempDirectory("");
        var resource = new ClassPathResource("solr");
        FileUtils.copyDirectory(resource.getFile(), solrTempDirectory.toFile());
        coreContainer =  new CoreContainer(solrTempDirectory.toString());
        coreContainer.load();
    }

    CoreContainer getCoreContainer() {
        return coreContainer;
    }

    public EmbeddedSolrServer createEmbeddedSolrServerInstance(String coreName) {
        return new EmbeddedSolrServer(coreContainer, coreName);
    }
}
