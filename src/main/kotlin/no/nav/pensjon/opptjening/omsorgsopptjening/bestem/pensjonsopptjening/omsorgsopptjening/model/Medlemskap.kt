package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import java.time.YearMonth

sealed class Medlemskap {

    abstract val kilde: DomainKilde

    data class Ja(
        override val kilde: DomainKilde
    ) : Medlemskap()

    data class Nei(
        override val kilde: DomainKilde
    ) : Medlemskap()

    data class Ukjent(
        override val kilde: DomainKilde,
    ) : Medlemskap()

    /**
     * Ikke helt avklart hvordan vi skal håndtere dette enda, ser ut som feltet "pensjonstrygdet" aldri har blitt brukt
     * som en mekanisme for filtrering. Inntil videre godtar vi alt"
     */
    fun erMedlem(): Boolean {
        return true
    }
}


data class Medlemskapsmåneder(
    private val m: Set<Medlemskapmåned>
) {
    val måneder get() = m.toSortedSet()
    infix fun merge(other: Medlemskapsmåneder): Medlemskapsmåneder {
        return Medlemskapsmåneder((måneder + other.måneder).toSet())
    }

    fun alleMåneder(): Set<YearMonth>{
        return måneder.map { it.måned }.toSet()
    }

    companion object {
        fun none(): Medlemskapsmåneder { return Medlemskapsmåneder(emptySet())}
    }
}

fun List<Medlemskapsmåneder>.merge(): Medlemskapsmåneder {
    return reduceOrNull { acc, medlemskapsmåneder -> acc.merge(medlemskapsmåneder) } ?: Medlemskapsmåneder.none()
}


data class Medlemskapmåned(
    val måned: YearMonth
): Comparable<Medlemskapmåned> {
    companion object {
        fun of(måned: YearMonth, medlemskap: Medlemskap): Medlemskapmåned? {
            return when (medlemskap.erMedlem()) {
                true -> Medlemskapmåned(måned)
                false -> null
            }
        }
    }

    override fun compareTo(other: Medlemskapmåned): Int {
        return måned.compareTo(other.måned)
    }
}