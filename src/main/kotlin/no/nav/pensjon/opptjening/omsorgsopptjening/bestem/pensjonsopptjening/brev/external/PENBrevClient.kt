package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.PENBrevMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external.PoppClient.Companion.logger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.time.Year
import java.util.*

@Component
class PENBrevClient(
@Value("\${PEN_BASE_URL}")
private val baseUrl: String,
@Qualifier("PENTokenProvider")
private val tokenProvider: TokenProvider,
private val penBrevMetricsMåling: PENBrevMetrikker,

) : BrevClient {
    private val restTemplate = RestTemplateBuilder().build()

    companion object {
        fun sendBrevPath(sakId: String) : String { return "/sak/$sakId/PE_OMSORG_HJELPESTOENAD_AUTO" }
    }


    override fun sendBrev(sakId: String, fnr: String, omsorgsår: Year, språk: BrevSpraak?): Journalpost {
        val url = baseUrl + sendBrevPath(sakId)
        println("sendBrev: url=$url")
        return try {
            penBrevMetricsMåling.oppdater {
                val brevRequest = serialize(SendBrevRequest(
                    omsorgsår,
                    UUID.randomUUID().toString(), // TODO: Fiks
                    språk
                ))
                println("brevRequest: $brevRequest")
                val response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    HttpEntity(
                        serialize(SendBrevRequest(
                                omsorgsår,
                                UUID.randomUUID().toString(), // TODO: Fiks
                                språk
                            )),
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
                Journalpost(mapper.readValue(response.body, SendBrevResponse::class.java).journalpostId)
            }
        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: $ex""".let {
                logger.warn(it, ex)
                throw BrevClientException("Feil ved kall til: $url", ex)
            }
        }
    }


    data class BrevData(val aarInvilgetOmsorgspoeng: Int)
    data class Overstyr(val spraak: BrevSpraak)

    data class SendBrevRequest(
//        val omsorgsår: Year,
        val brevdata: BrevData,
        val eksternReferanseId: String,
        @JsonInclude(JsonInclude.Include. NON_NULL)
        val overstyr: Overstyr?
    ) {
        constructor(omsorgsår: Year,eksternReferanseId: String,spraak: BrevSpraak? = null) :
                this(BrevData(omsorgsår.value),eksternReferanseId,spraak?.let { språk -> Overstyr(språk) })
    }


    private data class SendBrevResponse(
        val journalpostId: String
    )

    enum class BrevSpraak {
        EN, NB, NN
    }

    class PENBrevKlientException(message: String, throwable: Throwable) : RuntimeException(message, throwable)

}