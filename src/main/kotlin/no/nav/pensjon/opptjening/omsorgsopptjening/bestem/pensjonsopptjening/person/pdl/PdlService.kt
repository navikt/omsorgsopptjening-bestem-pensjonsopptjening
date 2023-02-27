package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person
import org.springframework.stereotype.Service

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {

    fun hentPerson(fnr: String): Person {
        val pdlResponse = pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)

        val pdlPerson = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

        val alleFnr =  pdlPerson.historisk() + pdlPerson.gjeldendeIdent()

        return Person(
            alleFnr = alleFnr,
            fodselsAr = pdlPerson.foedselsAr()
        )
    }

    private fun PdlPerson.historisk() = folkeregisteridentifikator
        .filter { it.status == Status.OPPHOERT }
        .distinctBy { it.identifikasjonsnummer }
        .map { Fnr(fnr = it.identifikasjonsnummer) }
        .toSet()

    private fun PdlPerson.gjeldendeIdent() =
        Fnr(
            fnr = folkeregisteridentifikator.firstOrNull { it.status == Status.I_BRUK }?.identifikasjonsnummer ?: throw PdlMottatDataException("Fnr i bruk finnes ikke"),
            gjeldende = true
        )

    private fun PdlPerson.foedselsAr(): Int =
        when (foedsel.size) {
            1 -> foedsel.first().foedselsaar
            else -> foedsel.avklarFoedsel()?.foedselsaar
                ?: throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
        }
}

class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL") {
    val code: PdlErrorCode? = pdlError?.extensions?.code
}

class PdlMottatDataException(message: String) : RuntimeException(message)