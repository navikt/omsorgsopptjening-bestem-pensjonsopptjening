package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.net.URI

internal class PdlClient(
    private val pdlUrl: String,
    private val tokenProvider: TokenProvider,
    private val metrics: PdlClientMetrics,
    private val graphqlQuery: GraphqlQuery,
    private val restTemplate: RestTemplate,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun hentPerson(fnr: String): PdlResponse? {
        val entity = RequestEntity<PdlQuery>(
            PdlQuery(graphqlQuery.hentPersonQuery(), FnrVariables(ident = fnr)),
            headers(),
            HttpMethod.POST,
            URI.create(pdlUrl)
        )

        val response = restTemplate.exchange(
            entity,
            PdlResponse::class.java
        ).body

        response?.error?.let { throw PdlException(it) }
        response?.warnings?.let { log.warn(it.toString()) }

        metrics.tellPersonHentet()
        return response
    }

    internal fun hentAktorId(fnr: String): IdenterResponse? {
        val entity = RequestEntity<PdlQuery>(
            PdlQuery(graphqlQuery.hentAktørIdQuery(), FnrVariables(ident = fnr)),
            headers(),
            HttpMethod.POST,
            URI.create(pdlUrl)
        )

        val response = restTemplate.exchange(
            entity,
            IdenterResponse::class.java
        ).body

        response?.error?.extensions?.code?.also {
            if (it == PdlErrorCode.SERVER_ERROR) throw PdlException(response.error)
        }
        metrics.tellAktørIdHentet()
        return response
    }

    private fun headers(): HttpHeaders {
        return HttpHeaders().apply {
            add("Nav-Call-Id", Mdc.getCorrelationId())
            add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
            add("Tema", "PEN")
            add("behandlingsnummer", "B300")
            add(CorrelationId.identifier, Mdc.getCorrelationId())
            add(InnlesingId.identifier, Mdc.getInnlesingId())
            accept = listOf(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(tokenProvider.getToken())
        }
    }
}

internal class GraphqlQuery(
    private val hentPersonQuery: Resource,
    private val hentAktorIdQuery: Resource
) {
    fun hentPersonQuery(): String {
        return String(hentPersonQuery.inputStream.readBytes()).replace("[\n\r]", "")
    }

    fun hentAktørIdQuery(): String {
        return String(hentAktorIdQuery.inputStream.readBytes()).replace("[\n\r]", "")
    }
}

private data class PdlQuery(val query: String, val variables: FnrVariables)

private data class FnrVariables(val ident: String)
