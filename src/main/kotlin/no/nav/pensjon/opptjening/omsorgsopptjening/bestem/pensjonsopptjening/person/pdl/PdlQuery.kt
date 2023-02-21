package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component


@Component
class GraphqlQuery(@Value("classpath:pdl/folkeregisteridentifikator.graphql") private val resourceFile: Resource) {
    fun getPersonFodselsaarQuery(): String {
        return String(resourceFile.file.readBytes()).replace("[\n\r]", "")
    }
}
data class PdlQuery(val query: String, val variables: FnrVariables)

data class FnrVariables(val ident: String)
