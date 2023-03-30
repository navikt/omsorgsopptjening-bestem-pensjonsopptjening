package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import org.springframework.stereotype.Service

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {

    fun hentPerson(fnr: String): PdlPerson {
        val pdlResponse = pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)

        val hentPersonQueryResponse = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

        val gjeldende = hentPersonQueryResponse.gjeldendeIdent()
        val historisk = hentPersonQueryResponse.historisk().filter { it.fnr !=  gjeldende.fnr}

        return PdlPerson(
            alleFnr = historisk + gjeldende,
            fodselsAr = hentPersonQueryResponse.foedselsAr()
        )
    }

    private fun HentPersonQueryResponse.historisk() = folkeregisteridentifikator
        .filter { it.status == Status.OPPHOERT }
        .distinctBy { it.identifikasjonsnummer }
        .map { PdlFnr(it.identifikasjonsnummer, gjeldende = false)}

    private fun HentPersonQueryResponse.gjeldendeIdent() =
        folkeregisteridentifikator
            .firstOrNull { it.status == Status.I_BRUK }
            ?.let{PdlFnr(it.identifikasjonsnummer, gjeldende = true)}
            ?: throw PdlMottatDataException("Fnr i bruk finnes ikke")


    private fun HentPersonQueryResponse.foedselsAr(): Int =
        when (foedsel.size) {
            1 -> foedsel.first().foedselsaar
            else -> foedsel.avklarFoedsel()?.foedselsaar
                ?: throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
        }
}

data class PdlPerson(val alleFnr: List<PdlFnr>, val fodselsAr: Int){

    val gjeldendeFnr: String get() = alleFnr.first { it.gjeldende }.fnr
    val historiskeFnr: List<String> get() = alleFnr.filter { !it.gjeldende }.map { it.fnr }

}

data class PdlFnr(val fnr: String, val gjeldende: Boolean)

class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL") {
    val code: PdlErrorCode? = pdlError?.extensions?.code
}

class PdlMottatDataException(message: String) : RuntimeException(message)