package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.toJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class BestemSakKlient(
    @Value("\${BESTEMSAK_URL}") private val bestemSakUrl: String,
    private val restTemplate: RestTemplate,
    private val registry: MeterRegistry
)
{
    private val antallSakerHentet = registry.counter("saker", "antall", "hentet")
    private val logger: Logger by lazy { LoggerFactory.getLogger(BestemSakKlient::class.java) }
    private val mapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

    /**
     * Henter pesys sakID for en gitt aktørID og saktype
     *
     * https://confluence.adeo.no/pages/viewpage.action?pageId=294133957
     * */
    fun kallBestemSak(requestBody: BestemSakRequest): BestemSakResponse? {
        return try {
            logger.info("Kaller bestemSak i PESYS")
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val response = restTemplate.exchange(
                bestemSakUrl,
                HttpMethod.POST,
                HttpEntity(requestBody.toJson(), headers),
                String::class.java)
            antallSakerHentet.increment()
            mapper.readValue(response.body, BestemSakResponse::class.java)
        } catch (ex: HttpStatusCodeException) {
            throw RuntimeException("En feil oppstod under kall til bestemSak i PESYS ex: ", ex)
        } catch (ex: Exception) {
            throw RuntimeException("En feil oppstod under kall til bestemSak i PESYS ex: ", ex)
        }
    }
}

data class BestemSakRequest(val aktoerId: String,
                            val ytelseType: SakType,
                            val callId: UUID,
                            val consumerId: UUID)

class BestemSakResponse(val feil: BestemSakFeil? = null,
                        val sakInformasjonListe: List<SakInformasjon>)

class BestemSakFeil(val feilKode: String, val feilmelding: String)

