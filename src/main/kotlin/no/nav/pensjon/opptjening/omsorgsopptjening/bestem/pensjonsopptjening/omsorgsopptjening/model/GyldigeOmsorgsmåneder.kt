package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.YearMonth

/**
 * Alle måneder vurdert som gyldig.
 * En gyldig måned er en måned hvor det er overlapp mellom utbetaling, medlemskap og omsorg.
 */
data class GyldigeOmsorgsmåneder(
    val måneder: Set<YearMonth>
){
    fun alleMåneder() = måneder

    companion object {

        fun none(): GyldigeOmsorgsmåneder {
            return GyldigeOmsorgsmåneder(emptySet())
        }
        fun of(
            omsorgsmåneder: Omsorgsmåneder,
            medlemskapsmåneder: Medlemskapsmåneder,
            utbetalingsmåneder: Utbetalingsmåneder,
        ): GyldigeOmsorgsmåneder {
            return GyldigeOmsorgsmåneder(
                omsorgsmåneder.alleMåneder().intersect(medlemskapsmåneder.alleMåneder()).intersect(utbetalingsmåneder.alleMåneder())
            )
        }
    }
}