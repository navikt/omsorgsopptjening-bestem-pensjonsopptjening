package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper
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
class BestemSakKlient(
    @Value("\${PEN_BASE_URL}") private val bestemSakUrl: String,
    @Qualifier("PENTokenProvider") private val tokenProvider: TokenProvider,
    registry: MeterRegistry
) {
    private val antallSakerHentet = registry.counter("saker", "antall", "hentet")
    private val logger: Logger by lazy { LoggerFactory.getLogger(BestemSakKlient::class.java) }
    private val restTemplate = RestTemplateBuilder().build()

    /**
     * Henter pesys sakID for en gitt aktørID og saktype
     *
     * https://confluence.adeo.no/pages/viewpage.action?pageId=294133957
     * */
    fun bestemSak(aktørId: String): Omsorgssak {
        val url = "$bestemSakUrl/api/bestemsak/v1"
        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                HttpEntity(
                    serialize(BestemSakRequest(aktørId)),
                    HttpHeaders().apply {
                        add("Nav-Call-Id", Mdc.getCorrelationId())
                        add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                        add(CorrelationId.identifier, Mdc.getCorrelationId())
                        add(InnlesingId.identifier, Mdc.getInnlesingId())
                        accept = listOf(MediaType.APPLICATION_JSON)
                        contentType = MediaType.APPLICATION_JSON
                        setBearerAuth(tokenProvider.getToken())
                    }
                ),
                String::class.java
            )
            antallSakerHentet.increment()
            mapper.readValue(response.body, BestemSakResponse::class.java).omsorgssak.let {
                Omsorgssak(
                    sakId = it.sakId,
                    enhet = it.saksbehandlendeEnhetId
                )
            }
        } catch (ex: Exception) {
            """Feil ved kall til $url, feil: ${ex::class.qualifiedName}""".let {
                logger.warn(it)
                throw BestemSakClientException(it, ex)
            }
        }
    }

    private data class BestemSakRequest(
        val aktoerId: String,
    ) {
        val ytelseType: SakType = SakType.OMSORG
        val callId: String = Mdc.getCorrelationId()
        val consumerId: String = "omsorgsopptjening-bestem-pensjonsopptjening"
    }

    private class BestemSakResponse(
        val feil: BestemSakFeil?,
        val sakInformasjonListe: List<SakInformasjon>
    ) {
        init {
            require(feil == null) { "Feil i respons fra bestem sak: $feil" }
        }

        val omsorgssak = sakInformasjonListe.singleOrNull { it.sakType == SakType.OMSORG }
            ?: throw RuntimeException("Klarte ikke å identifisere unik omsorgssak blandt: $sakInformasjonListe")
    }

    private data class BestemSakFeil(
        val feilKode: String,
        val feilmelding: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class SakInformasjon(
        val sakId: String,
        val sakType: SakType,
        val sakStatus: SakStatus,
        val saksbehandlendeEnhetId: String = "",
        val nyopprettet: Boolean = false,

        @JsonIgnore
        val tilknyttedeSaker: List<SakInformasjon> = emptyList()
    )

    private enum class SakType {
        ALDER,
        UFOREP,
        GJENLEV,
        BARNEP,
        OMSORG,
        GENRL
    }

    private enum class SakStatus {
        TIL_BEHANDLING,
        AVSLUTTET,
        LOPENDE,
        OPPHOR,
        OPPRETTET,
        UKJENT
    }
}

data class Omsorgssak(
    val sakId: String,
    val enhet: String,
)

class BestemSakClientException(message: String, throwable: Throwable) : RuntimeException(message, throwable)


