package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import pensjon.opptjening.azure.ad.client.TokenProvider

@Component
class PoppClient(
    @Value("\${POPP_URL}") private val url: String,
    @Qualifier("poppTokenProvider") private val tokenProvider: TokenProvider,
) {
    private val restTemplate = RestTemplateBuilder().build()

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun lagre(
        omsorgsyter: String,
        omsorgsÅr: Int,
        omsorgstype: DomainOmsorgstype,
        kilde: DomainKilde,
        omsorgsmottaker: String,
    ) {
        return send(
            Omsorg(
                fnr = omsorgsyter,
                ar = omsorgsÅr,
                omsorgType = PoppOmsorgType.from(omsorgstype),
                kilde = PoppKilde.from(kilde),
                fnrOmsorgFor = omsorgsmottaker
            )
        )
    }

    private fun send(omsorg: Omsorg) {
        val requestBody = serialize(LagreOmsorgRequest(omsorg))
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
        try {
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                LagreOmsorgRespons::class.java
            )
        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: $ex""".let {
                logger.warn(it)
                throw PoppClientExecption(it, ex)
            }
        }
    }
}

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
    /** Omsorg for syke/funksjonshemmede/eldre  */
    OSFE,

    /** Omsorg for barn over  6 år med hjelpestønad sats 3 eller 4  */
    OBO6H,

    /** Omsorg for barn under 6 år - eget vedtak  */
    OBU6;

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