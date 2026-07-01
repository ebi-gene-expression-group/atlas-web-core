package uk.ac.ebi.atlas.configuration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public final class RestTemplateFactory {
    private static final int MAX_TIMEOUT_MILLIS = 20_000;

    private RestTemplateFactory() {
    }

    /**
     * RestTemplate backed by {@link java.net.HttpURLConnection}, which uses JVM proxy system properties.
     * Call {@link HttpProxySettings#applyFromEnvironmentIfSystemPropertiesAbsent()} so {@code HTTP_PROXY}
     * from the environment is honoured in addition to {@code JAVA_TOOL_OPTIONS}/{@code CATALINA_OPTS}.
     */
    public static RestTemplate createDefault() {
        HttpProxySettings.applyFromEnvironmentIfSystemPropertiesAbsent();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(MAX_TIMEOUT_MILLIS);
        requestFactory.setConnectTimeout(MAX_TIMEOUT_MILLIS);

        return new RestTemplate(requestFactory);
    }
}
