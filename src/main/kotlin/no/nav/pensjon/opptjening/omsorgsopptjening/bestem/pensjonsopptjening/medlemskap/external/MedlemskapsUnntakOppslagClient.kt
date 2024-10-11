package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.MedlemskapsunntakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import org.apache.commons.lang3.ObjectUtils.min
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.net.URI
import java.time.LocalDate
import java.time.YearMonth

internal class MedlemskapsUnntakOppslagClient(
    private val url: String,
    private val tokenProvider: TokenProvider,
) : MedlemskapsUnntakOppslag {

    private val restTemplate = RestTemplateBuilder().build()

    override fun hentUnntaksperioder(
        fnr: String,
        fraOgMed: YearMonth,
        tilOgMed: YearMonth,
    ): Medlemskapsunntak {
        return hent(fnr, fraOgMed, tilOgMed)
    }

    private fun hent(
        fnr: String,
        fraOgMed: YearMonth,
        tilOgMed: YearMonth,
    ): Medlemskapsunntak {
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

        val mapped = deserialize<List<ResponsePeriode>>(response)
            .filter { setOf(PeriodestatusMedl.GYLD, PeriodestatusMedl.UAVK).contains(it.status) }

        return Medlemskapsunntak(
            ikkeMedlem = mapped
                .filter { !it.medlem }
                .map {
                    MedlemskapsunntakPeriode(
                        fraOgMed = YearMonth.of(it.fraOgMed.year, it.fraOgMed.month),
                        tilOgMed = min(
                            YearMonth.of(it.tilOgMed.year, it.tilOgMed.month),
                            tilOgMed
                        ) //trunkerer pg default 9999-12-31 for åpne perioder
                    )
                }
                .toSet(),
            pliktigEllerFrivillig = mapped
                .filter { it.medlem }
                .map {
                    MedlemskapsunntakPeriode(
                        fraOgMed = YearMonth.of(it.fraOgMed.year, it.fraOgMed.month),
                        tilOgMed = min(
                            YearMonth.of(it.tilOgMed.year, it.tilOgMed.month),
                            tilOgMed
                        ) //trunkerer pg default 9999-12-31 for åpne perioder
                    )
                }
                .toSet(),
            rådata = response
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ResponsePeriode(
    val unntakId: Int,
    val ident: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate, //alltid satt, 9999-12-31 tilsvarer null/åpen/ubestemt periode
    val status: PeriodestatusMedl,
    val statusaarsak: String,
    val dekning: String,
    val helsedel: Boolean,
    val medlem: Boolean,
    val lovvalgsland: String,
    val lovvalg: String,
    val grunnlag: String,
)

enum class PeriodestatusMedl {
    AVST, //avvist
    GYLD, //gyldig
    UAVK, //uavklart
}