package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.PENBrevMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevSpraak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.EksternReferanseId
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.time.Year

internal class PENBrevClient(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider,
    private val penBrevMetricsMåling: PENBrevMetrikker,
    private val restTemplate: RestTemplate,
) : BrevClient {

    companion object {
        fun createPath(baseUrl: String, sakId: String): String {
            return "$baseUrl/api/brev/sak/$sakId/PE_OMSORG_HJELPESTOENAD_AUTO"
        }

        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun sendBrev(
        sakId: String,
        eksternReferanseId: EksternReferanseId,
        omsorgsår: Year,
        språk: BrevSpraak?
    ): Journalpost {
        val url = createPath(baseUrl, sakId)
        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                HttpEntity(
                    serialize(
                        SendBrevRequest(
                            omsorgsår,
                            eksternReferanseId.value,
                            språk,
                        )
                    ),
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
            when (response.statusCode.value()) {
                200 -> {
                    mapper.readValue(
                        response.body,
                        SendBrevResponse.JournalPostId::class.java
                    ).let { response ->
                        if (response.error != null) {
                            throw BrevClientException("Brevtjenesten svarte ok, med journalId:${response.journalpostId} og feil, teknisk grunn:${response.error.tekniskgrunn} og beskrivelse: ${response.error.beskrivelse}")
                        }
                        Journalpost(response.journalpostId).also {
                            penBrevMetricsMåling.oppdater { it }
                        }
                    }
                }

                404 -> throw BrevClientException("Vedtak eksisterer ikke (400 Not Found)")
                400 -> {
                    val feil =
                        mapper.readValue(
                            response.body,
                            SendBrevResponse.Feil::class.java
                        )
                    throw BrevClientException("${feil.error.tekniskgrunn} ${feil.error.beskrivelse ?: ""}}")
                }

                else -> throw BrevClientException("PEN Brev returnerte http ${response.statusCode.value()}")
            }
        } catch (ex: HttpClientErrorException) {
            when (ex.statusCode.value()) {
                400 -> {
                    mapper.readValue(
                        ex.responseBodyAsString,
                        SendBrevResponse.Feil::class.java
                    ).let { feil ->
                        "Feil fra brevtjenesten: teknisk grunn: ${feil.error.tekniskgrunn}, beskrivelse: ${feil.error.beskrivelse}"
                            .let { message ->
                                throw BrevClientException(message, ex)
                            }
                    }
                }

                404 -> {
                    throw BrevClientException("Feil fra brevtjenesten: vedtak finnes ikke")
                }

                else -> throw BrevClientException("PEN Brev returnerte http ${ex.statusCode.value()}", ex)
            }
        } catch (ex: BrevClientException) {
            throw ex
        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: ${ex::class.qualifiedName}""".let {
                log.error(it)
                throw BrevClientException("Feil ved kall til: $url", ex)
            }
        }
    }

    data class BrevData(val aarInnvilgetOmsorgspoeng: Int)
    data class Overstyr(val spraak: BrevSpraak)

    data class SendBrevRequest(
        val brevdata: BrevData,
        val eksternReferanseId: String,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val overstyr: Overstyr?
    ) {
        constructor(omsorgsår: Year, eksternReferanseId: String, spraak: BrevSpraak? = null) :
                this(BrevData(omsorgsår.value), eksternReferanseId, spraak?.let { språk -> Overstyr(språk) })
    }

    private sealed class SendBrevResponse {
        data class JournalPostId(val journalpostId: String, val error: Error?) : SendBrevResponse()
        data object IkkeFunnet
        data class Feil(val error: Error)
        data class Error(val tekniskgrunn: String, val beskrivelse: String?)
    }
}