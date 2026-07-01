package uk.ac.ebi.atlas.configuration;

import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * Reads HTTP(S) proxy settings from JVM system properties or {@code HTTP_PROXY}/{@code NO_PROXY} env vars.
 * {@link org.springframework.web.client.RestTemplate} uses {@link java.net.HttpURLConnection}, which honours
 * {@code http.proxyHost}, {@code https.proxyHost}, and {@code http.nonProxyHosts} only (not {@code HTTP_PROXY}).
 */
public final class HttpProxySettings {
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private final String host;
    private final int port;
    private final String nonProxyHosts;

    private HttpProxySettings(String host, int port, String nonProxyHosts) {
        this.host = host;
        this.port = port;
        this.nonProxyHosts = nonProxyHosts;
    }

    public boolean enabled() {
        return StringUtils.hasText(host) && port > 0;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String nonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Ensures {@link java.net.HttpURLConnection} proxy system properties are set when the pod only exports
     * {@code HTTP_PROXY}/{@code NO_PROXY} (e.g. from the ebi-proxy ConfigMap).
     */
    public static void applyFromEnvironmentIfSystemPropertiesAbsent() {
        if (StringUtils.hasText(System.getProperty(HTTP_PROXY_HOST))) {
            return;
        }

        String httpProxy = firstNonBlank(System.getenv("HTTP_PROXY"), System.getenv("http_proxy"));
        if (!StringUtils.hasText(httpProxy)) {
            return;
        }

        try {
            URI uri = URI.create(httpProxy);
            if (!StringUtils.hasText(uri.getHost())) {
                return;
            }
            int port = uri.getPort() > 0 ? uri.getPort() : 80;
            System.setProperty(HTTP_PROXY_HOST, uri.getHost());
            System.setProperty(HTTP_PROXY_PORT, Integer.toString(port));
            System.setProperty(HTTPS_PROXY_HOST, uri.getHost());
            System.setProperty(HTTPS_PROXY_PORT, Integer.toString(port));
        } catch (IllegalArgumentException ignored) {
            return;
        }

        if (!StringUtils.hasText(System.getProperty(HTTP_NON_PROXY_HOSTS))) {
            String noProxy = firstNonBlank(System.getenv("NO_PROXY"), System.getenv("no_proxy"));
            if (StringUtils.hasText(noProxy)) {
                System.setProperty(HTTP_NON_PROXY_HOSTS, noProxy.replace(',', '|'));
            }
        }
    }

    public static HttpProxySettings fromSystemProperties() {
        applyFromEnvironmentIfSystemPropertiesAbsent();

        String nonProxyHosts = trimToNull(System.getProperty(HTTP_NON_PROXY_HOSTS));
        String httpsHost = trimToNull(System.getProperty(HTTPS_PROXY_HOST));
        if (httpsHost != null) {
            int httpsPort = parsePortOrDefault(System.getProperty(HTTPS_PROXY_PORT), 443);
            return new HttpProxySettings(httpsHost, httpsPort, nonProxyHosts);
        }

        String httpHost = trimToNull(System.getProperty(HTTP_PROXY_HOST));
        if (httpHost != null) {
            int httpPort = parsePortOrDefault(System.getProperty(HTTP_PROXY_PORT), 80);
            return new HttpProxySettings(httpHost, httpPort, nonProxyHosts);
        }

        return new HttpProxySettings(null, -1, nonProxyHosts);
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

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return null;
    }
}
