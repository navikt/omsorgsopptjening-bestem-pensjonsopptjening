package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

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
        val response = restTemplate.postForEntity(
            pdlUrl,
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            PdlResponse::class.java
        ).body

        response?.error?.extensions?.code?.also {
            if (it == PdlErrorCode.SERVER_ERROR) throw PdlException(response.error)
        }
        antallPersonerHentet.increment()
        return response
    }

    internal fun hentAktorId(graphqlQuery: String, fnr: String): IdenterResponse? {
        val response = restTemplate.postForEntity(
            pdlUrl,
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            IdenterResponse::class.java
        ).body

        response?.error?.extensions?.code?.also {
            if (it == PdlErrorCode.SERVER_ERROR) throw PdlException(response.error)
        }
        antallAktoridHentet.increment()
        return response
    }
}