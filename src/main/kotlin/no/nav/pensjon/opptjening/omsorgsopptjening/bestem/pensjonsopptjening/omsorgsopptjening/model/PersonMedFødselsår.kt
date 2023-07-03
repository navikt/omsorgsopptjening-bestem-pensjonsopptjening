package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.LocalDate
import java.time.Month

class PersonMedFødselsår(
    val fnr: String,
    val fodselsAr: Int
) {
    override fun equals(other: Any?) = this === other || (other is PersonMedFødselsår && this.fnr == other.fnr)
    override fun hashCode() = fnr.hashCode()

    fun alderVedUtløpAv(aarstall: Int): Int {
        return aarstall - fodselsAr
    }

    fun erFødt(årstall: Int): Boolean {
        return alderVedUtløpAv(årstall) == 0
    }

    fun erFødt(årstall: Int, måned: Month): Boolean {
        return fødselsdato().let { it.year == årstall && it.month == måned }
    }

    fun fødselsdato(): LocalDate {
        return LocalDate.of(fodselsAr, fnrMonth(), fnrDay())
    }

    private fun fnrDay(): Int {
        return Integer.parseInt(fnr.substring(0, 2))
    }
    private fun fnrMonth(): Month {
        return Month.of(Integer.parseInt(fnr.substring(2, 4)))
    }
}

