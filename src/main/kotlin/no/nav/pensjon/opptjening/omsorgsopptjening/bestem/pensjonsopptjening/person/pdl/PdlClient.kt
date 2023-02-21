package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class PdlClient(
    @Value("\${PDL_URL}") private val pdlUrl: String,
    private val restTemplate: RestTemplate
) {

    @Retryable(value = [RestClientException::class], backoff = Backoff(delay = 1000L, maxDelay = 170000L, multiplier = 3.0))
    fun hentPerson(graphqlQuery: String, fnr: String): PdlResponse? {
        return restTemplate.postForEntity(
            pdlUrl,
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            PdlResponse::class.java
        ).body
    }
}