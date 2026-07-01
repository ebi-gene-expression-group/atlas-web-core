package uk.ac.ebi.atlas.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

@Configuration
@Profile("!cli")
public class WebClientConfig {
    @Profile("!cli")
    @Bean
    public WebClient webClientWithProxy() {
        HttpClient httpClient = HttpClient.create();
        HttpProxySettings proxySettings = HttpProxySettings.fromSystemProperties();
        if (proxySettings.enabled()) {
            httpClient = httpClient.tcpConfiguration(tcpClient -> tcpClient.proxy(proxy -> {
                ProxyProvider.Builder builder = proxy.type(ProxyProvider.Proxy.HTTP)
                        .host(proxySettings.host())
                        .port(proxySettings.port());
                if (StringUtils.hasText(proxySettings.nonProxyHosts())) {
                    builder.nonProxyHosts(proxySettings.nonProxyHosts());
                }
            }));
        }
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
