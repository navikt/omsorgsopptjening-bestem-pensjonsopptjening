package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import java.time.YearMonth

data class Landstilknytningmåneder(
    val måneder: Set<LandstilknytningMåned>
) {
    fun alle(): Set<YearMonth> {
        return måneder.map { it.måned }.toSortedSet()
    }

    infix fun merge(other: Landstilknytningmåneder): Landstilknytningmåneder {
        require(måneder.intersect(other.måneder).isEmpty()) { "Kan ikke merge overlappende måneder" }
        return Landstilknytningmåneder((måneder + other.måneder).toSet())
    }

    fun erNorge(måned: YearMonth): Boolean {
        return forMåned(måned) is Landstilknytning.Norge
    }

    fun erEøs(måned: YearMonth): Boolean {
        return forMåned(måned) is Landstilknytning.Eøs
    }

    fun erNorge(måneder: Set<YearMonth>): Boolean {
        return måneder.all { erNorge(it) }
    }

    fun erEøs(måneder: Set<YearMonth>): Boolean {
        return måneder.all { erEøs(it) }
    }

    fun forMåned(måned: YearMonth): Landstilknytning? {
        return måneder.singleOrNull { it.måned == måned }?.landstilknytning
    }

    fun eøsMåneder(): Set<LandstilknytningMåned> {
        return måneder.filter { erEøs(it.måned) }.toSet()
    }

    fun alleEøsMåneder(): Set<YearMonth> {
        return eøsMåneder().map { it.måned }.toSet()
    }

    companion object {
        fun none(): Landstilknytningmåneder {
            return Landstilknytningmåneder(emptySet())
        }
    }
}

data class LandstilknytningMåned(
    val måned: YearMonth,
    val landstilknytning: Landstilknytning
)