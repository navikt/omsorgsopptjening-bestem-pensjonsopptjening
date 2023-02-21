package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.stereotype.Service

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {


    fun hentPerson(fnr: String) {
        pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)
    }
}