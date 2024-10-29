package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external.PENBrevClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.PENBrevMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
class BrevClientConfig {

    @Bean
    fun brevClient(
        @Value("\${PEN_BASE_URL}") baseUrl: String,
        @Qualifier("PENTokenProvider") tokenProvider: TokenProvider,
        penBrevMetricsMåling: PENBrevMetrikker,
        restTemplate: RestTemplate,
    ): BrevClient {
        return PENBrevClient(
            baseUrl = baseUrl,
            tokenProvider = tokenProvider,
            penBrevMetricsMåling = penBrevMetricsMåling,
            restTemplate = restTemplate,
        )
    }
}