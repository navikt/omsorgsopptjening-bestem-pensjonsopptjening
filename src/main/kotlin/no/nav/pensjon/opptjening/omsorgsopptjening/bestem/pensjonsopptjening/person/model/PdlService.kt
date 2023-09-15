package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.Doedsfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.ForelderBarnRelasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.GraphqlQuery
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.HentPersonQueryResponse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.IdentGruppe
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlError
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlErrorCode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.Status
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.avklarFoedsel
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PdlService(private val graphqlQuery: GraphqlQuery, private val pdlClient: PdlClient) {

    fun hentPerson(fnr: String): PdlPerson {
        val pdlResponse = pdlClient.hentPerson(graphqlQuery = graphqlQuery.getPersonFodselsaarQuery(), fnr = fnr)

        val hentPersonQueryResponse = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

        val gjeldende = hentPersonQueryResponse.gjeldendeIdent()
        val historisk = hentPersonQueryResponse.historisk().filter { it.fnr != gjeldende.fnr }

        return PdlPerson(
            alleFnr = historisk + gjeldende,
            fodselsdato = hentPersonQueryResponse.foedselsdato(),
            doedsdato = bestemDoedsdato(pdlResponse.data.hentPerson.doedsfall),
            forelderBarnRelasjon = hentPersonQueryResponse.forelderBarnRelasjon()
        )
    }

    fun hentAktorId(fnr: String): String {
        val pdlResponse = pdlClient.hentAktorId(graphqlQuery = graphqlQuery.getAktorIdQuery(), fnr = fnr)
        return pdlResponse?.data?.hentIdenter?.identer
            ?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }
            ?.let { it.ident }
            ?: throw java.lang.RuntimeException("Fant ingen aktørId")

    }

    private fun bestemDoedsdato(doedsfall: List<Doedsfall?>): LocalDate? {
        return doedsfall.firstOrNull()?.doedsdato
    }

    private fun HentPersonQueryResponse.historisk() = folkeregisteridentifikator
        .filter { it.status == Status.OPPHOERT }
        .distinctBy { it.identifikasjonsnummer }
        .map { PdlFnr(it.identifikasjonsnummer, gjeldende = false) }

    private fun HentPersonQueryResponse.gjeldendeIdent() =
        folkeregisteridentifikator
            .firstOrNull { it.status == Status.I_BRUK }
            ?.let { PdlFnr(it.identifikasjonsnummer, gjeldende = true) }
            ?: throw PdlMottatDataException("Fnr i bruk finnes ikke")


    private fun HentPersonQueryResponse.foedselsdato(): LocalDate {
        return when (foedsel.size) {
            0 -> {
                throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
            }

            1 -> {
                LocalDate.parse(foedsel.first().foedselsdato)
            }

            else -> {
                LocalDate.parse(foedsel.avklarFoedsel()?.foedselsdato)
                    ?: throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
            }
        }
    }

    private fun HentPersonQueryResponse.forelderBarnRelasjon(): List<ForelderBarnRelasjon>{
        return forelderBarnRelasjon
    }

}

data class PdlPerson(
    val alleFnr: List<PdlFnr>,
    val fodselsdato: LocalDate,
    val doedsdato: LocalDate? = null,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
) {
    val gjeldendeFnr: String get() = alleFnr.first { it.gjeldende }.fnr
    val historiskeFnr: List<String> get() = alleFnr.filter { !it.gjeldende }.map { it.fnr }
}

data class PdlFnr(val fnr: String, val gjeldende: Boolean)

class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL") {
    val code: PdlErrorCode? = pdlError?.extensions?.code
}

class PdlMottatDataException(message: String) : RuntimeException(message)