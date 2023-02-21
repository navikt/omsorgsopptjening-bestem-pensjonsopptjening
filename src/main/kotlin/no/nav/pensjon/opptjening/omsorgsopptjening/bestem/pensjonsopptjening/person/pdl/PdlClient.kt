package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PdlClient(
    @Value("\${PDL_URL}") private val pdlUrl: String,
    private val restTemplate: RestTemplate,
    private val query: GraphqlQuery
) {

    fun hentPerson(fnr: String): ResponseEntity<PdlResponse> {
        val request: PdlQuery = query.createPersonFodselsaarQuery(fnr)
        return restTemplate.postForEntity(pdlUrl, request, PdlResponse::class.java)
    }

}