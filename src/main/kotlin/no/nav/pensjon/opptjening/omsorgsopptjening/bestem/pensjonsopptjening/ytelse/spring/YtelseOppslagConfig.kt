package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse.PENYtelseOppslagClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse.YtelseOppslag
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
class YtelseOppslagConfig {

    @Bean
    fun ytelseOppslag(
        @Value($$"${PEN_BASE_URL}") penBaseUrl: String,
        @Qualifier("PENTokenProvider") tokenProvider: TokenProvider,
        restTemplate: RestTemplate,
    ): YtelseOppslag {
        return PENYtelseOppslagClient(
            baseUrl = penBaseUrl,
            tokenProvider = tokenProvider,
            restTemplate = restTemplate,
        )
    }
}