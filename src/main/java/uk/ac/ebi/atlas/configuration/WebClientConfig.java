package uk.ac.ebi.atlas.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

@Configuration
@Profile("!cli")
public class WebClientConfig {
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    @Profile("!cli")
    @Bean
    public WebClient webClientWithProxy() {
        HttpClient httpClient = HttpClient.create();
        ProxySettings proxySettings = ProxySettings.fromSystemProperties();
        if (proxySettings.enabled()) {
            httpClient = httpClient.tcpConfiguration(tcpClient -> tcpClient.proxy(proxy -> {
                ProxyProvider.Builder builder = proxy.type(ProxyProvider.Proxy.HTTP)
                        .host(proxySettings.host)
                        .port(proxySettings.port);
                if (StringUtils.hasText(proxySettings.nonProxyHosts)) {
                    builder.nonProxyHosts(proxySettings.nonProxyHosts);
                }
            }));
        }
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }



    private static final class ProxySettings {
        private final String host;
        private final int port;
        private final String nonProxyHosts;

        private ProxySettings(String host, int port, String nonProxyHosts) {
            this.host = host;
            this.port = port;
            this.nonProxyHosts = nonProxyHosts;
        }

        private boolean enabled() {
            return StringUtils.hasText(host) && port > 0;
        }

        private static ProxySettings fromSystemProperties() {
            String nonProxyHosts = trimToNull(System.getProperty(HTTP_NON_PROXY_HOSTS));
            String httpsHost = trimToNull(System.getProperty(HTTPS_PROXY_HOST));
            if (httpsHost != null) {
                int httpsPort = parsePortOrDefault(System.getProperty(HTTPS_PROXY_PORT), 443);
                return new ProxySettings(httpsHost, httpsPort, nonProxyHosts);
            }

            String httpHost = trimToNull(System.getProperty(HTTP_PROXY_HOST));
            if (httpHost != null) {
                int httpPort = parsePortOrDefault(System.getProperty(HTTP_PROXY_PORT), 80);
                return new ProxySettings(httpHost, httpPort, nonProxyHosts);
            }

            return new ProxySettings(null, -1, nonProxyHosts);
        }

        private static int parsePortOrDefault(String portValue, int defaultPort) {
            if (!StringUtils.hasText(portValue)) {
                return defaultPort;
            }
            try {
                int port = Integer.parseInt(portValue);
                return (port >= 1 && port <= 65535) ? port : defaultPort;
            } catch (NumberFormatException ignored) {
                return defaultPort;
            }
        }

        private static String trimToNull(String value) {
            if (!StringUtils.hasText(value)) {
                return null;
            }
            return value.trim();
        }
    }
}
