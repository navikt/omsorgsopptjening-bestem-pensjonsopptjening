package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.YearMonth

data class Ytelsemåneder(
    val måneder: Set<YearMonth>
) {
    fun alle(): Set<YearMonth> {
        return måneder.map { it }.toSortedSet()
    }

    infix fun merge(other: Ytelsemåneder): Ytelsemåneder {
        require(måneder.intersect(other.måneder).isEmpty()) { "Kan ikke merge overlappende måneder" }
        return Ytelsemåneder((måneder + other.måneder).toSet())
    }

    companion object {
        fun none(): Ytelsemåneder {
            return Ytelsemåneder(emptySet())
        }
    }
}