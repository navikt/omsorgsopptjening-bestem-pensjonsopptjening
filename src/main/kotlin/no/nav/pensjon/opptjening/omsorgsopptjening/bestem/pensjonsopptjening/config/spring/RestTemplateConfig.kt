package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.httpcomponents.hc5.PoolingHttpClientConnectionManagerMetricsBinder
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.io.HttpClientConnectionManager
import org.apache.hc.core5.util.TimeValue
import org.apache.hc.core5.util.Timeout
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.util.function.Supplier

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(
        clientHttpRequestFactory: Supplier<ClientHttpRequestFactory>
    ): RestTemplate {
        return RestTemplateBuilder().apply {
            requestFactory(clientHttpRequestFactory)
        }.build()
    }

    @Bean
    fun clientHttpRequestFactory(
        httpClientConnectionManager: HttpClientConnectionManager
    ): Supplier<ClientHttpRequestFactory> {
        return Supplier {
            HttpComponentsClientHttpRequestFactory().apply {
                httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.DEFAULT)
                    .setConnectionManager(httpClientConnectionManager)
                    .setConnectionManagerShared(true)
                    .build()
            }
        }
    }

    @Bean
    fun poolingConnectionManager(
        registry: MeterRegistry
    ): HttpClientConnectionManager {
        return PoolingHttpClientConnectionManager().apply {
            defaultMaxPerRoute = 10
            maxTotal = 64
            setDefaultConnectionConfig(
                ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(30))
                    .setSocketTimeout(Timeout.ofSeconds(30))
                    .setTimeToLive(TimeValue.ofSeconds(60))
                    .setValidateAfterInactivity(TimeValue.ofSeconds(10))
                    .build()
            )
            PoolingHttpClientConnectionManagerMetricsBinder(this, "httpClientConnectionMetricsBinder").bindTo(registry)
        }
    }
}