package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

fun createQuery(fnr: String): PdlQuery {
    val query = PdlQuery::class.java.getResource("/pdl/queries/folkeregisteridentifikator.graphql")
        .readText()
        .replace("[\n\r]", "")
    return PdlQuery(query, FnrVariables(fnr))
}

data class PdlQuery(val query: String, val variables: FnrVariables)

data class FnrVariables(val ident: String)
