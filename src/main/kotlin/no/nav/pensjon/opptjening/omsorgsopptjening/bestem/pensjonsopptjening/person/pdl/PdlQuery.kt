package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

data class PdlQuery(val query: String, val variables: FnrVariables)
data class FnrVariables(val ident: String)

data class PdlResponse(val data: PdlData?, private val errors: List<PdlError>? = null) {
    val error: PdlError? = errors?.firstOrNull()
}

data class PdlData(val hentPerson: Person?)

fun folkeregisteridentifikatorQuery(fnr: String): PdlQuery {
    val query = PdlQuery::class.java.getResource("/pdl/queries/folkeregisteridentifikator.graphql")
        .readText()
        .replace("[\n\r]", "")
    return PdlQuery(query, FnrVariables(fnr))
}
