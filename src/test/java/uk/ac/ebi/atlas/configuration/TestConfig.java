package uk.ac.ebi.atlas.configuration;

import com.google.common.collect.ImmutableSet;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.utils.UrlHelpers;

//@EnableCaching
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
    public UrlHelpers urlHelpers() {
        return new UrlHelpers() {};
    }

//    @Bean
//    public CacheManager cacheManager() {
//        SimpleCacheManager cacheManager = new SimpleCacheManager();
//        cacheManager.setCaches(
//                ImmutableSet.of(
//                        new ConcurrentMapCache("designElementsByGeneId"),
//                        new ConcurrentMapCache("arrayDesignByAccession"),
//                        new ConcurrentMapCache("experimentByAccession"),
//                        new ConcurrentMapCache("experimentsByType")));
//        cacheManager.initializeCaches();
//
//        return cacheManager;
//    }
}
