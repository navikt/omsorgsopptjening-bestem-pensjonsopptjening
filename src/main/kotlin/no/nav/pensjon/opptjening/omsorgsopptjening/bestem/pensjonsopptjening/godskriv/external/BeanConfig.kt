package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
internal class BeanConfig(
    @Value("\${POPP_URL}") private val baseUrl: String,
    @Qualifier("poppTokenProvider") private val tokenProvider: TokenProvider,
) {
    private val poppClient = PoppClient(
        baseUrl = baseUrl,
        tokenProvider = tokenProvider
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