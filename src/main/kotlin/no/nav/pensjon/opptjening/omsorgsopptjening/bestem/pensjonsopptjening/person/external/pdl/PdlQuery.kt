package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component


@Component
class GraphqlQuery(
    @Value("classpath:pdl/folkeregisteridentifikator.graphql")
    private val hentPersonQuery: Resource,
    @Value("classpath:pdl/hentAktorId.graphql")
    private val hentAktorIdQuery: Resource
) {
    fun getPersonFodselsaarQuery(): String {
        return String(hentPersonQuery.inputStream.readBytes()).replace("[\n\r]", "")
    }

    fun getAktorIdQuery(): String {
        return String(hentAktorIdQuery.inputStream.readBytes()).replace("[\n\r]", "")
    }
}

data class PdlQuery(val query: String, val variables: FnrVariables)

data class FnrVariables(val ident: String)