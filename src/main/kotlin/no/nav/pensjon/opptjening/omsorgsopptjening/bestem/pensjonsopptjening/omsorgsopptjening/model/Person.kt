package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.LocalDate
import java.time.Month

class Person(
    val fnr: String,
    val fødselsdato: LocalDate,
    val dødsdato: LocalDate?,
    val familierelasjoner: Familierelasjoner
) {
    override fun equals(other: Any?) = this === other || (other is Person && this.fnr == other.fnr)
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
}

