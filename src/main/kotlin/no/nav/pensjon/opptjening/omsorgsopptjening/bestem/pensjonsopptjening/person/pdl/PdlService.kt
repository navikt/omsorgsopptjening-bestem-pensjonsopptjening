package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.stereotype.Service

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {

    fun hentPerson(fnr: String): PdlPerson {
        val pdlResponse = pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)

        val hentPersonQueryResponse = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

        return PdlPerson(
            gjeldendeFnr = hentPersonQueryResponse.gjeldendeIdent(),
            historiskeFnr = hentPersonQueryResponse.historisk(),
            fodselsAr = hentPersonQueryResponse.foedselsAr()
        )
    }

    private fun HentPersonQueryResponse.historisk() = folkeregisteridentifikator
        .filter { it.status == Status.OPPHOERT }
        .distinctBy { it.identifikasjonsnummer }
        .map { it.identifikasjonsnummer }

    private fun HentPersonQueryResponse.gjeldendeIdent() =
        folkeregisteridentifikator
            .firstOrNull { it.status == Status.I_BRUK }?.identifikasjonsnummer ?: throw PdlMottatDataException("Fnr i bruk finnes ikke")


    private fun HentPersonQueryResponse.foedselsAr(): Int =
        when (foedsel.size) {
            1 -> foedsel.first().foedselsaar
            else -> foedsel.avklarFoedsel()?.foedselsaar
                ?: throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
        }
}

data class PdlPerson(val gjeldendeFnr: String, val historiskeFnr: List<String>, val fodselsAr: Int)

class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL") {
    val code: PdlErrorCode? = pdlError?.extensions?.code
}

class PdlMottatDataException(message: String) : RuntimeException(message)