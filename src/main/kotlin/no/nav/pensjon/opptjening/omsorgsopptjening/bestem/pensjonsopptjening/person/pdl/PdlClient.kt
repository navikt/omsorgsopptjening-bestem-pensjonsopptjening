package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class PdlClient(
    @Value("\${PDL_URL}") private val pdlUrl: String,
    private val restTemplate: RestTemplate,
    private val registry: MeterRegistry
) {
    private val antallPersonerHentet = registry.counter("personer", "antall", "hentet")
    private val antallAktoridHentet = registry.counter("aktorid", "antall", "hentet")

    @Retryable(
        maxAttempts = 4,
        value = [RestClientException::class, PdlException::class],
        backoff = Backoff(delay = 1500L, maxDelay = 30000L, multiplier = 2.5)
    )
    fun hentPerson(graphqlQuery: String, fnr: String): PdlResponse? {
        val entity = RequestEntity<PdlQuery>(
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            HttpHeaders().apply {
                this.add(CorrelationId.name, Mdc.getOrCreateCorrelationId())
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

    internal fun hentAktorId(graphqlQuery: String, fnr: String): IdenterResponse? {
        val entity = RequestEntity<PdlQuery>(
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            HttpHeaders().apply {
                this.add(CorrelationId.name, Mdc.getOrCreateCorrelationId())
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