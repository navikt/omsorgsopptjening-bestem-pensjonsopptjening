package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BestemSakKlient(
    @Value("\${BESTEMSAK_URL}") private val bestemSakUrl: String,
    private val restTemplate: RestTemplate,
    private val registry: MeterRegistry
) {
    private val antallSakerHentet = registry.counter("saker", "antall", "hentet")
    private val logger: Logger by lazy { LoggerFactory.getLogger(BestemSakKlient::class.java) }

    /**
     * Henter pesys sakID for en gitt aktørID og saktype
     *
     * https://confluence.adeo.no/pages/viewpage.action?pageId=294133957
     * */
    fun bestemSak(aktørId: String): Omsorgssak {
        return try {
            logger.info("Kaller bestemSak i PESYS")
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val response = restTemplate.exchange(
                bestemSakUrl,
                HttpMethod.POST,
                HttpEntity(serialize(BestemSakRequest(aktørId)), headers),
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
            """Feil ved kall til ${bestemSakUrl}, feil: $ex""".let {
                logger.warn(it)
                throw BestemSakClientException(it, ex)
            }
        }
    }

    private data class BestemSakRequest(
        val aktoerId: String,
    ) {
        val ytelseType: SakType = SakType.OMSORG
        val callId: String = Mdc.getOrCreateCorrelationId()
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


