package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import java.time.YearMonth

sealed class Omsorgsmåneder {
    protected abstract val måneder: Set<YearMonth>
    protected val sortert get() = måneder.toSortedSet()

    fun alle(): Set<YearMonth> {
        return sortert
    }

    fun antall(): Int {
        return alle().count()
    }

    fun omsorgstype(): DomainOmsorgskategori {
        return when (this) {
            is Barnetrygd -> DomainOmsorgskategori.BARNETRYGD
            is Hjelpestønad -> DomainOmsorgskategori.HJELPESTØNAD
        }
    }

    data class Barnetrygd(
        override val måneder: Set<YearMonth>
    ) : Omsorgsmåneder() {
        fun merge(other: Barnetrygd): Barnetrygd {
            return Barnetrygd((sortert + other.sortert).toSet())
        }

        companion object {
            fun of(måned: YearMonth, omsorgstype: DomainOmsorgstype.Barnetrygd): Barnetrygd {
                return when (omsorgstype) {
                    DomainOmsorgstype.Barnetrygd.Delt -> none()
                    DomainOmsorgstype.Barnetrygd.Full -> Barnetrygd(setOf(måned))
                }
            }

            fun none(): Barnetrygd {
                return Barnetrygd(emptySet())
            }
        }
    }

    data class Hjelpestønad(
        override val måneder: Set<YearMonth>
    ) : Omsorgsmåneder() {
        fun merge(other: Hjelpestønad): Hjelpestønad {
            return Hjelpestønad((sortert + other.sortert).toSet())
        }

        companion object {
            fun none(): Hjelpestønad {
                return Hjelpestønad(emptySet())
            }
        }
    }
}