package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person
import org.springframework.stereotype.Service

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {

    fun hentPerson(fnr: String): Person {
        val pdlResponse = pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)

        return Person(
            pdlResponse!!.gjeldendeIdent(),
            pdlResponse.historisk()
        )
    }

    private fun PdlResponse.historisk() = data!!.hentPerson!!.folkeregisteridentifikator
        .filter { it.status == Status.OPPHOERT }
        .distinctBy { it.identifikasjonsnummer }
        .map { Fnr(it.identifikasjonsnummer) }
        .toSet()

    private fun PdlResponse.gjeldendeIdent() =
        Fnr(data!!.hentPerson!!.folkeregisteridentifikator.first { it.status == Status.I_BRUK }.identifikasjonsnummer)
}



