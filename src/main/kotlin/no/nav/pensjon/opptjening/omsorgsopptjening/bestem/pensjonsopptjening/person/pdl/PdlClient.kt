package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PdlClient(
    @Value("\${PDL_URL}") private val pdlUrl: String,
    private val restTemplate: RestTemplate
) {

    fun hentPerson(graphqlQuery: String, fnr: String): ResponseEntity<PdlResponse> {
        return restTemplate.postForEntity(
            pdlUrl,
            PdlQuery(graphqlQuery, FnrVariables(ident = fnr)),
            PdlResponse::class.java
        )
    }
}