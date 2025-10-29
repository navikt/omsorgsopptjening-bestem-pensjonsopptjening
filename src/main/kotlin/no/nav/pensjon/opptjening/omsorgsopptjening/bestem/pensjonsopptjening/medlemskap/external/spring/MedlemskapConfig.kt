package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external.MedlemskapsUnntakOppslagClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider

@Configuration
class MedlemskapConfig {

    @Bean
    fun medlemskapsClient(
        @Value($$"${MEDLEMSKAP_URL}") url: String,
        @Qualifier("medlemskapTokenProvider") tokenProvider: TokenProvider,
        restTemplate: RestTemplate,
    ): MedlemskapsUnntakOppslag {
        return MedlemskapsUnntakOppslagClient(
            url = url,
            tokenProvider = tokenProvider,
            restTemplate = restTemplate
        )
    }
}