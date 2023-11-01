package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import java.time.YearMonth

data class Utbetalingsmåneder(
    private val m: Set<Utbetalingsmåned>
) {
    val måneder get() = m.toSortedSet()
    infix fun merge(other: Utbetalingsmåneder): Utbetalingsmåneder {
        return Utbetalingsmåneder((måneder + other.måneder).toSet())
    }

    fun alleMåneder(): Set<YearMonth> {
        return måneder.map { it.måned }.toSet()
    }

    companion object {
        fun none() = Utbetalingsmåneder(emptySet())
    }
}

/**
 * En måned hvor det er lagt til grunn at man har mottatt barnetrygd
 */
data class Utbetalingsmåned(
    val måned: YearMonth,
    val utbetalt: Int,
    val landstilknytning: Landstilknytning
) : Comparable<Utbetalingsmåned> {

    companion object {
        fun of(måned: YearMonth, utbetalt: Int, landstilknytning: Landstilknytning): Utbetalingsmåned? {
            return when {
                landstilknytning is Landstilknytning.Eøs.NorgeSekundærland && utbetalt >= 0 -> {
                    Utbetalingsmåned(måned, utbetalt, landstilknytning)
                }

                landstilknytning is Landstilknytning.Eøs.UkjentPrimærOgSekundærLand && utbetalt > 0 -> {
                    Utbetalingsmåned(måned, utbetalt, landstilknytning)
                }

                landstilknytning is Landstilknytning.Norge && utbetalt > 0 -> {
                    Utbetalingsmåned(måned, utbetalt, landstilknytning)
                }

                else -> {
                    null
                }
            }
        }

        fun none(): Utbetalingsmåneder {
            return Utbetalingsmåneder(emptySet())
        }

        fun List<Utbetalingsmåneder>.merge(): Utbetalingsmåneder {
            return reduceOrNull { acc, utbetalingsmåneder -> acc merge utbetalingsmåneder } ?: Utbetalingsmåneder(
                emptySet()
            )
        }
    }

    override fun compareTo(other: Utbetalingsmåned): Int {
        return måned.compareTo(other.måned)
    }
}


