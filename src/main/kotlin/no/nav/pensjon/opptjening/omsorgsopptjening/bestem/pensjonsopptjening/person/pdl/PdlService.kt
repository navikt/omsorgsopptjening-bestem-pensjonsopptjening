package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person
import org.springframework.stereotype.Service

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {

    fun hentPerson(fnr: String): Person {
        val pdlResponse = pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)

        val pdlPerson = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

        return Person(
            pdlPerson.gjeldendeIdent(),
            pdlPerson.historisk()
        )
    }

    private fun PdlPerson.historisk() = folkeregisteridentifikator
        .filter { it.status == Status.OPPHOERT }
        .distinctBy { it.identifikasjonsnummer }
        .map { Fnr(it.identifikasjonsnummer) }
        .toSet()

    private fun PdlPerson.gjeldendeIdent() = Fnr(folkeregisteridentifikator.first { it.status == Status.I_BRUK }.identifikasjonsnummer)
}

class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL")
// TODO Utvid med kode



