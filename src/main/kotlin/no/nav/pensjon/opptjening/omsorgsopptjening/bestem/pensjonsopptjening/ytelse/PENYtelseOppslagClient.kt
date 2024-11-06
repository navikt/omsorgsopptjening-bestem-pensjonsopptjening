package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.YtelsePeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.YtelseType
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelseinformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.KanSlåsSammen.Companion.slåSammenLike
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.net.URI
import java.time.LocalDate
import java.time.YearMonth

class PENYtelseOppslagClient(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider,
    private val restTemplate: RestTemplate,
) : YtelseOppslag {

    override fun hentLøpendeAlderspensjon(fnr: String, fraOgMed: YearMonth, tilOgMed: YearMonth): Ytelseinformasjon {
        val fomDato = fraOgMed.atDay(1)
        val tomDato = tilOgMed.atEndOfMonth()

        val entity = RequestEntity<String>(
            serialize(
                AlderYtelseRequest(
                    fnr = fnr,
                    fomDato = fomDato,
                    tomDato = tomDato
                ),
            ),
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            },
            HttpMethod.POST,
            URI.create("$baseUrl/api/alderspensjon/vedtak/gjeldende")
        )

        val response = restTemplate.exchange(entity, String::class.java).body!!

        val mapped = deserialize<AlderYtelseResponse>(response)
            .vedtakListe
            .map { if (it.gjelderTomDato == null) it.copy(gjelderTomDato = tomDato) else it } //truncate in case of open ended tom

        return Ytelseinformasjon(
            perioder = mapped
                .map { it.periode }.slåSammenLike()
                .map {
                    YtelsePeriode(
                        fom = it.min(),
                        tom = it.max(),
                        type = YtelseType.ALDERSPENSJON
                    )
                }.toSet(),
            rådata = response

        )
    }

    private data class AlderYtelseRequest(
        val fnr: String,
        val fomDato: LocalDate,
        val tomDato: LocalDate,
    )

    private data class AlderYtelseResponse(
        val vedtakListe: List<AlderYtelsePeriode>
    )

    private data class AlderYtelsePeriode(
        val gjelderFomDato: LocalDate,
        val gjelderTomDato: LocalDate?
    ) {
        init {
            require(gjelderFomDato.isBefore(gjelderTomDato)) { "Fom ikke tidligere enn tom" }
        }

        val periode: Periode get() = Periode(YearMonth.of(gjelderFomDato.year, gjelderFomDato.month), YearMonth.of(gjelderTomDato!!.year, gjelderTomDato.month))
    }


    override fun hentLøpendeUføretrygd(fnr: String, fraOgMed: YearMonth, tilOgMed: YearMonth): Ytelseinformasjon {
        val fomDato = fraOgMed.atDay(1)
        val tomDato = tilOgMed.atEndOfMonth()

        val entity = RequestEntity<String>(
            serialize(
                UføreYtelseRequest(
                    pid = fnr,
                    fom = fomDato,
                    tom = tomDato
                )
            ),
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            },
            HttpMethod.POST,
            URI.create("$baseUrl/api/uforetrygd/vedtak/gjeldende")
        )

        val response = restTemplate.exchange(entity, String::class.java).body!!

        val mapped = deserialize<UføreYtelseResponse>(response)
            .uforeperioder
            .map { if (it.tom == null) it.copy(tom = tomDato) else it } //truncate in case of open ended tom

        return Ytelseinformasjon(
            perioder = mapped
                .map { it.periode }.slåSammenLike()
                .map {
                    YtelsePeriode(
                        fom = it.min(),
                        tom = it.max(),
                        type = YtelseType.UFØRETRYGD
                    )
                }.toSet(),
            rådata = response
        )
    }

    private data class UføreYtelseRequest(
        val pid: String,
        val fom: LocalDate,
        val tom: LocalDate,
    )

    private data class UføreYtelseResponse(
        val uforeperioder: List<UføreYtelsePeriode>
    )

    private data class UføreYtelsePeriode(
        val fom: LocalDate,
        val tom: LocalDate?
    ) {
        init {
            require(fom.isBefore(tom)) { "Fom ikke tidligere enn tom" }
        }

        val periode: Periode get() = Periode(YearMonth.of(fom.year, fom.month), YearMonth.of(tom!!.year, tom.month))
    }
}

