package uk.ac.ebi.atlas.configuration;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.io.SolrClientCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@PropertySource("classpath:solr.properties")
public class SolrConfig {
    public static final Random RNG = ThreadLocalRandom.current();
    public static final SolrClientCache SOLR_CLIENT_CACHE = new SolrClientCache();

    @Bean
    public SolrClient solrClientBioentities(@Value("${solr.hosts}") String[] solrHosts) {
        return new HttpSolrClient
                .Builder(solrHosts[RNG.nextInt(solrHosts.length)] + "/atlas-bioentities")
                .build();
    }

    @Bean
    public CloudSolrClient cloudSolrClient(@Value("${zk.hosts}") String[] zkHosts) {
        return new CloudSolrClient
                .Builder(ImmutableList.copyOf(zkHosts), Optional.empty())
                .build();
    }

    @Bean
    public CloudSolrClient cloudSolrClient(@Value("${zk.host}") String zkHost,
                                           @Value("${zk.port}") String zkPort) {
        return new CloudSolrClient.Builder().withZkHost(zkHost + ":" + zkPort).build();
    }
}
