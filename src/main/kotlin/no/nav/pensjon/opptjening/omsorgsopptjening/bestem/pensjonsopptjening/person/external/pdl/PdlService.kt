package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslagException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
internal class PdlService(
    private val pdlClient: PdlClient
) : PersonOppslag {

    override fun hentPerson(fnr: String): Person {
        try {
            val pdlResponse = pdlClient.hentPerson(fnr = fnr)

            val hentPersonQueryResponse = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

            val gjeldende = hentPersonQueryResponse.gjeldendeIdent()
            val historisk = hentPersonQueryResponse.historisk().filter { it.fnr != gjeldende.fnr }

            return PdlPerson(
                alleFnr = historisk + gjeldende,
                fodselsdato = hentPersonQueryResponse.foedselsdato(),
                doedsdato = bestemDoedsdato(pdlResponse.data.hentPerson.doedsfall),
                forelderBarnRelasjon = hentPersonQueryResponse.forelderBarnRelasjon()
            ).let { person ->
                Person(
                    fnr = person.gjeldendeFnr,
                    fødselsdato = person.fodselsdato,
                    dødsdato = person.doedsdato,
                    familierelasjoner = person.forelderBarnRelasjon
                        .map {
                            Familierelasjon(
                                ident = it.relatertPersonsIdent,
                                relasjon = when (it.relatertPersonsRolle) {
                                    "BARN" -> Familierelasjon.Relasjon.BARN
                                    "FAR" -> Familierelasjon.Relasjon.FAR
                                    "MOR" -> Familierelasjon.Relasjon.MOR
                                    "MEDMOR" -> Familierelasjon.Relasjon.MEDMOR
                                    else -> throw RuntimeException("Ukjent familierelasjon: ${it.relatertPersonsRolle}")
                                }
                            )
                        }.let {
                            Familierelasjoner(it)
                        }
                )
            }
        } catch (ex: Throwable) {
            throw PersonOppslagException("Feil ved henting av person", ex)
        }

    }

    override fun hentAktørId(fnr: String): String {
        try {
            val pdlResponse = pdlClient.hentAktorId(fnr = fnr)
            return pdlResponse?.data?.hentIdenter?.identer
                ?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }
                ?.let { it.ident }
                ?: throw RuntimeException("Fant ingen aktørId")

        } catch (ex: Throwable) {
            throw PersonOppslagException("Feil ved henting av aktørid", ex)
        }
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

    private fun HentPersonQueryResponse.forelderBarnRelasjon(): List<ForelderBarnRelasjon> {
        return forelderBarnRelasjon
    }

}

internal data class PdlPerson(
    val alleFnr: List<PdlFnr>,
    val fodselsdato: LocalDate,
    val doedsdato: LocalDate? = null,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
) {
    val gjeldendeFnr: String get() = alleFnr.first { it.gjeldende }.fnr
    val historiskeFnr: List<String> get() = alleFnr.filter { !it.gjeldende }.map { it.fnr }
}

internal data class PdlFnr(val fnr: String, val gjeldende: Boolean)

internal class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL") {
    val code: PdlErrorCode? = pdlError?.extensions?.code
}

internal class PdlMottatDataException(message: String) : RuntimeException(message)