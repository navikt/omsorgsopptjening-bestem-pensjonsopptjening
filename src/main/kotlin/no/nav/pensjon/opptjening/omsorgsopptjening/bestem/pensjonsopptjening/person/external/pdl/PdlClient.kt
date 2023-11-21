package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.net.URI

@Component
internal class PdlClient(
    @Value("\${PDL_URL}") private val pdlUrl: String,
    @Qualifier("pdlTokenProvider") private val tokenProvider: TokenProvider,
    registry: MeterRegistry,
    private val graphqlQuery: GraphqlQuery,
) {
    private val antallPersonerHentet = registry.counter("personer", "antall", "hentet")
    private val antallAktoridHentet = registry.counter("aktorid", "antall", "hentet")
    private val restTemplate = RestTemplateBuilder().build()

    @Retryable(
        maxAttempts = 4,
        value = [RestClientException::class, PdlException::class],
        backoff = Backoff(delay = 1500L, maxDelay = 30000L, multiplier = 2.5)
    )
    fun hentPerson(fnr: String): PdlResponse? {
        val entity = RequestEntity<PdlQuery>(
            PdlQuery(graphqlQuery.hentPersonQuery(), FnrVariables(ident = fnr)),
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                add("Tema", "PEN")
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            },
            HttpMethod.POST,
            URI.create(pdlUrl)
        )

        val response = restTemplate.exchange(
            entity,
            PdlResponse::class.java
        ).body

        response?.error?.extensions?.code?.also {
            if (it == PdlErrorCode.SERVER_ERROR) throw PdlException(response.error)
        }
        antallPersonerHentet.increment()
        return response
    }

    internal fun hentAktorId(fnr: String): IdenterResponse? {
        val entity = RequestEntity<PdlQuery>(
            PdlQuery(graphqlQuery.hentAktørIdQuery(), FnrVariables(ident = fnr)),
            HttpHeaders().apply {
                add("Nav-Call-Id", Mdc.getCorrelationId())
                add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
                add("Tema", "PEN")
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            },
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
        antallAktoridHentet.increment()
        return response
    }
}

@Component
internal class GraphqlQuery(
    @Value("classpath:pdl/folkeregisteridentifikator.graphql")
    private val hentPersonQuery: Resource,
    @Value("classpath:pdl/hentAktorId.graphql")
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
