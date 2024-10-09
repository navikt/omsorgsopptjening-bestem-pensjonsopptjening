package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
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
import java.time.LocalDate
import java.time.YearMonth

@Component
internal class MedlemskapOppslagClient(
    @Value("\${MEDLEMSKAP_URL}") private val url: String,
    @Qualifier("medlemskapTokenProvider") private val tokenProvider: TokenProvider,
) : MedlemskapOppslag {

    private val restTemplate = RestTemplateBuilder().build()

    override fun hentMedlemskapsgrunnlag(
        fnr: String,
        fraOgMed: YearMonth,
        tilOgMed: YearMonth,
    ): Medlemskapsgrunnlag {
        return hent(fnr, fraOgMed, tilOgMed)
    }

    @Retryable(
        maxAttempts = 4,
        value = [RestClientException::class],
        backoff = Backoff(delay = 1500L, maxDelay = 30000L, multiplier = 2.5)
    )
    private fun hent(
        fnr: String,
        fraOgMed: YearMonth,
        tilOgMed: YearMonth,
    ): Medlemskapsgrunnlag {
        val fomDato = fraOgMed.atDay(1)
        val tomDato = tilOgMed.atEndOfMonth()

        val entity = RequestEntity<Void>(
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add("Nav-Personident", fnr)
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            },
            HttpMethod.GET,
            URI.create("$url/api/v1/medlemskapsunntak?fraOgMed=$fomDato&tilOgMed=$tomDato")
        )

        val response = restTemplate.exchange(entity, String::class.java).body!!

        val mapped = deserialize<List<Unntak>>(response)

        return Medlemskapsgrunnlag(
            unntaksperioder = mapped.map {
                Medlemskapsgrunnlag.Unntaksperiode(
                    fraOgMed = YearMonth.of(it.fraOgMed.year, it.fraOgMed.month),
                    tilOgMed = YearMonth.of(it.tilOgMed.year, it.tilOgMed.month)
                )
            },
            rådata = response
        )
    }
}


internal data class Unntak(
    val unntakId: Int,
    val ident: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val status: String,
    val statusaarsak: String,
    val dekning: String,
    val helsedel: Boolean,
    val medlem: Boolean,
    val lovvalgsland: String,
    val lovvalg: String,
    val grunnlag: String,
    val sporingsinformasjon: Sporingsinformasjon,
    val studieinformasjon: Studieinformasjon
)

internal data class Sporingsinformasjon(
    val versjon: Int,
    val registrert: String,
    val besluttet: String,
    val kilde: String,
    val kildedokument: String,
    val opprettet: String,
    val opprettetAv: String,
    val sistEndret: String,
    val sistEndretAv: String
)

internal data class Studieinformasjon(
    val statsborgerland: String,
    val studieland: String,
    val delstudie: Boolean,
    val soeknadInnvilget: Boolean
)