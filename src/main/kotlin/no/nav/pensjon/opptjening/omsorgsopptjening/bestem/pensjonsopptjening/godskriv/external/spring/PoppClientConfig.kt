package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external.PoppClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
internal class PoppClientConfig(
    @Value($$"${POPP_URL}") private val baseUrl: String,
    @Qualifier("poppTokenProvider") private val tokenProvider: TokenProvider,
    restTemplate: RestTemplate,
) {
    private val poppClient = PoppClient(
        baseUrl = baseUrl,
        tokenProvider = tokenProvider,
        restTemplate = restTemplate,
    )

    @Bean("godskrivOpptjening")
    fun godskrivOpptjening(): GodskrivOpptjeningClient {
        return poppClient
    }

    @Bean("hentPensjonspoeng")
    fun hentPensjonspoeng(): HentPensjonspoengClient {
        return poppClient
    }
}