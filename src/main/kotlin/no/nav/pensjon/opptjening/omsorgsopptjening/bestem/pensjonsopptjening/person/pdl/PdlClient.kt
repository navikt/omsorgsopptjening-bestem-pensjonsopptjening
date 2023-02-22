package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalTime

@Component
class PdlClient(
    @Value("\${PDL_URL}") private val pdlUrl: String,
    private val restTemplate: RestTemplate
) {

    @Retryable(
        maxAttempts = 4,
        value = [RestClientException::class, PdlException::class],
        backoff = Backoff(delay = 1500L, maxDelay = 30000L, multiplier = 2.5)
    )
    fun hentPerson(graphqlQuery: String, fnr: String): PdlResponse? {
        println("Henter person: ${LocalTime.now()}")
        val response = restTemplate.postForEntity(
            pdlUrl,
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            PdlResponse::class.java
        ).body

        response?.error?.extensions?.code?.also {
            if (it == PdlErrorCode.SERVER_ERROR) throw PdlException(response.error)
        }

        return response
    }
}