package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import java.time.YearMonth

sealed class Omsorgsmåneder {
    protected abstract val måneder: Set<YearMonth>
    protected val sortert get() = måneder.toSortedSet()

    fun alleMåneder(): Set<YearMonth>{
        return sortert
    }

    fun omsorgstype(): DomainOmsorgstype {
        return when(this){
            is Barnetrygd -> DomainOmsorgstype.BARNETRYGD
            is Hjelpestønad -> DomainOmsorgstype.HJELPESTØNAD
        }
    }

    data class Barnetrygd(
        override val måneder: Set<YearMonth>
    ) : Omsorgsmåneder() {
        fun merge(other: Barnetrygd): Barnetrygd {
            return Barnetrygd((sortert + other.sortert).toSet())
        }

        companion object {
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