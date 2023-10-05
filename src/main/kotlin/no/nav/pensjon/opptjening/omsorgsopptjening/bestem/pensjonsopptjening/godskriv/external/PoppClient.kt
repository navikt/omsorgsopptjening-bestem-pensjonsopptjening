package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Pensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import pensjon.opptjening.azure.ad.client.TokenProvider

internal class PoppClient(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider,
) : GodskrivOpptjeningClient, HentPensjonspoengClient {
    private val restTemplate = RestTemplateBuilder().build()

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun godskriv(
        omsorgsyter: String,
        omsorgsÅr: Int,
        omsorgstype: DomainOmsorgstype,
        kilde: DomainKilde,
        omsorgsmottaker: String
    ) {
        val requestBody = serialize(
            LagreOmsorgRequest(
                Omsorg(
                    fnr = omsorgsyter,
                    ar = omsorgsÅr,
                    omsorgType = PoppOmsorgType.from(omsorgstype),
                    kilde = PoppKilde.from(kilde),
                    fnrOmsorgFor = omsorgsmottaker
                )
            )
        )
        val httpEntity = HttpEntity(
            requestBody,
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            }
        )
        val url = "$baseUrl/omsorg"
        try {
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                LagreOmsorgRespons::class.java
            )
        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: $ex""".let {
                logger.warn(it, ex)
                throw PoppClientExecption(it, ex)
            }
        }
    }

    override fun hentPensjonspoengForOmsorgstype(fnr: String, år: Int, type: DomainOmsorgstype): Pensjonspoeng.Omsorg {
        val url = "$baseUrl/pensjonspoeng?fomAr=$år&tomAr=$år&pensjonspoengType=${PoppOmsorgType.from(type)}"
        try {
            return restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity(
                    null,
                    HttpHeaders().apply {
                        add("fnr", fnr)
                        add("Nav-Call-Id", Mdc.getCorrelationId())
                        add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                        add(CorrelationId.identifier, Mdc.getCorrelationId())
                        add(InnlesingId.identifier, Mdc.getInnlesingId())
                        accept = listOf(MediaType.APPLICATION_JSON)
                        contentType = MediaType.APPLICATION_JSON
                        setBearerAuth(tokenProvider.getToken())
                    }
                ),
                HentPensjonspoengResponse::class.java,
            ).let {
                when {
                    it.body == null -> Pensjonspoeng.Omsorg(år = år, poeng = 0.0, type = type)
                    it.body!!.pensjonspoeng == null -> Pensjonspoeng.Omsorg(år = år, poeng = 0.0, type = type)
                    it.body!!.pensjonspoeng!!.isEmpty() -> Pensjonspoeng.Omsorg(år = år, poeng = 0.0, type = type)
                    else -> it.body!!.omsorgspoeng(år = år, type = PoppOmsorgType.from(type))
                }
            }

        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: $ex""".let {
                logger.warn(it, ex)
                throw HentPensjonspoengClientException(it)
            }
        }
    }

    override fun hentPensjonspoengForInntekt(fnr: String, år: Int): Pensjonspoeng.Inntekt {
        val url = "$baseUrl/pensjonspoeng?fomAr=$år&tomAr=$år&pensjonspoengType=${PoppOmsorgType.PPI}"
        try {
            return restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity(
                    null,
                    HttpHeaders().apply {
                        add("fnr", fnr)
                        add("Nav-Call-Id", Mdc.getCorrelationId())
                        add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                        add(CorrelationId.identifier, Mdc.getCorrelationId())
                        add(InnlesingId.identifier, Mdc.getInnlesingId())
                        accept = listOf(MediaType.APPLICATION_JSON)
                        contentType = MediaType.APPLICATION_JSON
                        setBearerAuth(tokenProvider.getToken())
                    }
                ),
                HentPensjonspoengResponse::class.java,
            ).let {
                when {
                    it.body == null -> Pensjonspoeng.Inntekt(år = år, poeng = 0.0)
                    it.body!!.pensjonspoeng == null -> Pensjonspoeng.Inntekt(år = år, poeng = 0.0)
                    it.body!!.pensjonspoeng!!.isEmpty() -> Pensjonspoeng.Inntekt(år = år, poeng = 0.0)
                    else -> it.body!!.inntektspoeng(år = år)
                }
            }

        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: $ex""".let {
                logger.warn(it, ex)
                throw HentPensjonspoengClientException(it)
            }
        }
    }
}

private data class HentPensjonspoengResponse(
    val pensjonspoeng: List<Poeng>?
) {
    fun omsorgspoeng(år: Int, type: PoppOmsorgType): Pensjonspoeng.Omsorg {
        return pensjonspoeng!!.single { it.ar == år && it.pensjonspoengType == type }.let {
            Pensjonspoeng.Omsorg(
                år = it.ar,
                poeng = it.poeng,
                type = it.pensjonspoengType.toDomain()
            )
        }
    }

    fun inntektspoeng(år: Int): Pensjonspoeng.Inntekt {
        return pensjonspoeng!!.single { it.ar == år }.let {
            Pensjonspoeng.Inntekt(
                år = it.ar,
                poeng = it.poeng,
            )
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Poeng(
    val ar: Int,
    val poeng: Double,
    val pensjonspoengType: PoppOmsorgType
)

private data class LagreOmsorgRequest(val omsorg: Omsorg)
private class LagreOmsorgRespons

private data class Omsorg(
    val fnr: String,
    val ar: Int,
    val omsorgType: PoppOmsorgType,
    val kilde: PoppKilde,
    val fnrOmsorgFor: String?,
)

private enum class PoppKilde {
    /**
     * Infotrygd
     */
    IT,

    /**
     * Barnetrygd Systemet
     */
    BA;

    companion object {
        fun from(domainKilde: DomainKilde): PoppKilde {
            return when (domainKilde) {
                DomainKilde.BARNETRYGD -> BA
                DomainKilde.INFOTRYGD -> IT
            }
        }
    }
}

private enum class PoppOmsorgType {
    /**
     * Pensjonspoeng for pensjonsgivende inntekt.
     */
    PPI,

    /** Omsorg for syke/funksjonshemmede/eldre  */
    OSFE,

    /** Omsorg for barn over  6 år med hjelpestønad sats 3 eller 4  */
    OBO6H,

    /** Omsorg for barn under 6 år - eget vedtak  */
    OBU6;

    fun toDomain(): DomainOmsorgstype {
        return when (this) {
            OSFE -> TODO("Never mapped")
            OBO6H -> DomainOmsorgstype.HJELPESTØNAD
            OBU6 -> DomainOmsorgstype.BARNETRYGD
            PPI -> TODO("Never mapped")
        }
    }

    companion object {
        fun from(domainOmsorgstype: DomainOmsorgstype): PoppOmsorgType {
            return when (domainOmsorgstype) {
                DomainOmsorgstype.BARNETRYGD -> OBU6
                DomainOmsorgstype.HJELPESTØNAD -> OBO6H
            }
        }
    }
}

class PoppClientExecption(message: String, throwable: Throwable) : RuntimeException(message, throwable)