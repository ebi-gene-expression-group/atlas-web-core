package uk.ac.ebi.atlas.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("cli")
@Configuration
public class WebClientCliConfig {
    @Bean
    @Profile("cli")
    public WebClient webClient() {        
        return WebClient.builder()
                .build();
    } 
}
