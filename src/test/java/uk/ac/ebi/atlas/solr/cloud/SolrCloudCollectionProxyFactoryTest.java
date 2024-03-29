package uk.ac.ebi.atlas.solr.cloud;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SolrCloudCollectionProxyFactoryTest {
    private static class NullaryConstructorCollectionProxy<T extends CollectionProxy<?>> extends CollectionProxy<T> {
        NullaryConstructorCollectionProxy() {
            super(null, null);
        }
    }

    private static class TwoArgConstructorCollectionProxy<T extends CollectionProxy<?>> extends CollectionProxy<T> {
        TwoArgConstructorCollectionProxy(SolrClient solrClient, String name) {
            super(solrClient, name);
        }
    }

    private SolrCloudCollectionProxyFactory subject;

    @BeforeEach
    void setUp() {
        subject = new SolrCloudCollectionProxyFactory(new CloudSolrClient.Builder().withZkHost("foobar").build());
    }

    @ParameterizedTest
    @MethodSource("collectionProxyTypeProvider")
    void createSupportedCollectionProxies(Class<? extends CollectionProxy<?>> type) {
        assertThat(subject.create(type)).isInstanceOf(type);
    }

    @Test
    void exceptionsOfIncorrectArgsConstructorsAreWrapped() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subject.create(NullaryConstructorCollectionProxy.class))
                .withCauseInstanceOf(NoSuchMethodException.class);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> subject.create(TwoArgConstructorCollectionProxy.class))
                .withCauseInstanceOf(NoSuchMethodException.class);
    }

    private static Stream<Class<? extends CollectionProxy<?>>> collectionProxyTypeProvider() {
        return Stream.of(BioentitiesCollectionProxy.class);
    }
}
