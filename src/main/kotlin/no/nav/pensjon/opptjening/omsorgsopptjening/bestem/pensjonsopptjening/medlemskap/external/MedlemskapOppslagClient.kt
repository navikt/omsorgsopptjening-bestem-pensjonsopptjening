package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.net.URI

@Component
internal class MedlemskapOppslagClient(
    @Value("\${MEDLEMSKAP_URL}") private val url: String,
    @Qualifier("medlemskapTokenProvider") private val tokenProvider: TokenProvider,
) : MedlemskapOppslag {

    private val restTemplate = RestTemplateBuilder().build()

    override fun hentMedlemskapsgrunnlag(
        fnr: String
    ): Medlemskapsgrunnlag {
        return hent(fnr)
    }

    @Retryable(
        maxAttempts = 4,
        value = [RestClientException::class],
        backoff = Backoff(delay = 1500L, maxDelay = 30000L, multiplier = 2.5)
    )
    private fun hent(
        fnr: String
    ): Medlemskapsgrunnlag {
        val entity = RequestEntity<MedlemskapRequest>(
            MedlemskapRequest(fnr),
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            },
            HttpMethod.POST,
            URI.create(url)
        )

        val response = restTemplate.exchange(
            entity,
            MedlemskapResponse::class.java
        ).body!!

        return Medlemskapsgrunnlag(
            vurderingFraLoveME = when(response.resultat.svar){
                Svar.JA -> Medlemskapsgrunnlag.LoveMeVurdering.MEDLEM_I_FOLKETRYGDEN
                Svar.NEI -> Medlemskapsgrunnlag.LoveMeVurdering.IKKE_MEDLEM_I_FOLKETRYGDEN
                Svar.UAVKLART -> Medlemskapsgrunnlag.LoveMeVurdering.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN
            },
            rådata = jacksonObjectMapper().writeValueAsString(response)

        )
    }
}

private data class MedlemskapRequest(val fnr: String)

private data class MedlemskapResponse(val resultat: Resultat)

private data class Resultat(val svar: Svar)

private enum class Svar {
    JA,
    NEI,
    UAVKLART,
}