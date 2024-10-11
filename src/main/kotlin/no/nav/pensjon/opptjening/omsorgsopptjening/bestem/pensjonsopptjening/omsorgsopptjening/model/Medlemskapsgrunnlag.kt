package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.YearMonth

data class Medlemskapsgrunnlag(
    val medlemskapsunntak: Medlemskapsunntak,
) {
    fun alleMånederUtenMedlemskap(): Set<YearMonth> {
        return medlemskapsunntak.ikkeMedlem.flatMap { it.periode.alleMåneder() }.toSet()
    }

    fun alleMånederMedMedlemskap(): Set<YearMonth> {
        return medlemskapsunntak.pliktigEllerFrivillig.flatMap { it.periode.alleMåneder() }.toSet()
    }

    fun avgrensForÅr(år: Int): Medlemskapsgrunnlag {
        return Medlemskapsgrunnlag(
            medlemskapsunntak = medlemskapsunntak.avgrensForÅr(år),
        )
    }
}
