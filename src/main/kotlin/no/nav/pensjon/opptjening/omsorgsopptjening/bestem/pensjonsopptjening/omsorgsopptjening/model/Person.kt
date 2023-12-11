package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.lang.RuntimeException
import java.time.LocalDate
import java.time.Month

class Person(
    val fødselsdato: LocalDate,
    val dødsdato: LocalDate?,
    val familierelasjoner: Familierelasjoner,
    val identhistorikk: IdentHistorikk,
) {
    val fnr = identhistorikk.gjeldende().ident

    override fun equals(other: Any?) = this === other || (other is Person && identifisertAv(other.fnr))
    override fun hashCode() = fnr.hashCode()

    fun alderVedUtløpAv(aarstall: Int): Int {
        return aarstall - fødselsdato.year
    }

    fun erFødt(årstall: Int): Boolean {
        return alderVedUtløpAv(årstall) == 0
    }

    fun erFødt(årstall: Int, måned: Month): Boolean {
        return fødselsdato().let { it.year == årstall && it.month == måned }
    }

    fun fødselsdato(): LocalDate {
        return fødselsdato
    }

    fun erBarnAv(fnr: String): Boolean {
        return familierelasjoner.erForelder(fnr)
    }

    fun erForelderAv(fnr: String): Boolean {
        return familierelasjoner.erBarn(fnr)
    }

    fun finnForeldre(): Foreldre {
        return familierelasjoner.finnForeldre()
    }

    fun identifisertAv(fnr: String): Boolean {
        return identhistorikk.identifiseresAv(fnr)
    }
}


class IdentHistorikk(
    private val identer: Set<Ident.FolkeregisterIdent>
) {
    fun gjeldende(): Ident.FolkeregisterIdent {
        return identer.singleOrNull { it is Ident.FolkeregisterIdent.Gjeldende }
            ?: throw IdentHistorikkManglerGjeldendeException()
    }

    fun historikk(): Set<Ident.FolkeregisterIdent> {
        return identer
    }

    fun identifiseresAv(ident: String): Boolean {
        return identer.map { it.ident }.contains(ident)
    }

    class IdentHistorikkManglerGjeldendeException(msg: String = "Fant ingen gjeldende identer i identhistorikk") :
        RuntimeException(msg)
}
