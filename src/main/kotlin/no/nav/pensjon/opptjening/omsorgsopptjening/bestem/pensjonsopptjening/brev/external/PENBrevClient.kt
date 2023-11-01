package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

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

@Component
private class PENBrevClient(
    @Value("\${PEN_BASE_URL}") private val baseUrl: String,
    @Qualifier("PENTokenProvider") private val tokenProvider: TokenProvider,
    private val penBrevMetricsMåling: PENBrevMetrikker,

    ) : BrevClient {
    private val restTemplate = RestTemplateBuilder().build()

    override fun sendBrev(
        sakId: String,
        fnr: String,
        omsorgsår: Int
    ): Journalpost {
        val url = "$baseUrl/api/bestillbrev/todo"
        return try {
            penBrevMetricsMåling.oppdater {
                val response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    HttpEntity(
                        serialize(
                            SendBrevRequest(
                                sakId = sakId,
                                fnr = fnr,
                                omsorgsår = omsorgsår
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
                Journalpost(mapper.readValue(response.body, SendBrevResponse::class.java).journalpostId)
            }
        } catch (ex: Throwable) {
            """Feil ved kall til $url, feil: $ex""".let {
                logger.warn(it, ex)
                throw BrevClientException("Feil ved kall til: $url", ex)
            }
        }
    }

    private data class SendBrevRequest(
        val sakId: String,
        val fnr: String,
        val omsorgsår: Int
    )

    private data class SendBrevResponse(
        val journalpostId: String
    )
}