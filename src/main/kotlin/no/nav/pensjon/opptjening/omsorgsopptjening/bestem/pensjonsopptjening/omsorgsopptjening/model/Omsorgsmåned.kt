package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import java.time.YearMonth

sealed class Omsorgsmåneder {
    abstract val omsorgsmåneder: Set<Omsorgsmåned>

    fun alle(): Set<YearMonth> {
        return omsorgsmåneder.map { it.måned }.toSortedSet()
    }

    fun antall(): Int {
        return alle().count()
    }

    fun omsorgstype(): DomainOmsorgskategori {
        return when (this) {
            is Barnetrygd -> DomainOmsorgskategori.BARNETRYGD
            is BarnetrygdOgHjelpestønad -> DomainOmsorgskategori.HJELPESTØNAD
        }
    }

    fun erKvalifisertForAutomatiskBehandling(antallMånederRegel: AntallMånederRegel): Boolean {
        return kvalifisererForAutomatiskBehandling().antall().oppfyller(antallMånederRegel)
    }

    fun erKvalifisertForManuellBehandling(antallMånederRegel: AntallMånederRegel): Boolean {
        return kvalifisererForManuellBehandling().antall().oppfyller(antallMånederRegel)
    }

    fun kvalifisererForAutomatiskBehandling(): Omsorgsmåneder {
        return when(this){
            is Barnetrygd -> Barnetrygd(full())
            is BarnetrygdOgHjelpestønad -> BarnetrygdOgHjelpestønad(full())
        }
    }

    fun kvalifisererForManuellBehandling(): Omsorgsmåneder {
        return when(this){
            is Barnetrygd -> Barnetrygd(full() + delt())
            is BarnetrygdOgHjelpestønad -> BarnetrygdOgHjelpestønad(full() + delt())
        }
    }

    fun full(): Set<Omsorgsmåned> {
        return omsorgsmåneder.filter { it.omsorgstype is DomainOmsorgstype.Barnetrygd.Full }.toSet()
    }

    fun antallFull(): Int {
        return full().count()
    }

    fun delt(): Set<Omsorgsmåned> {
        return omsorgsmåneder.filter { it.omsorgstype is DomainOmsorgstype.Barnetrygd.Delt }.toSet()
    }

    fun antallDelt(): Int {
        return delt().count()
    }

    data class Barnetrygd(
        override val omsorgsmåneder: Set<Omsorgsmåned>
    ) : Omsorgsmåneder() {

        fun merge(other: Barnetrygd): Barnetrygd {
            return Barnetrygd((omsorgsmåneder + other.omsorgsmåneder).toSet())
        }

        companion object {
            fun none(): Barnetrygd {
                return Barnetrygd(emptySet())
            }
        }
    }

    data class BarnetrygdOgHjelpestønad(
        override val omsorgsmåneder: Set<Omsorgsmåned>,
    ) : Omsorgsmåneder() {

        fun merge(other: BarnetrygdOgHjelpestønad): BarnetrygdOgHjelpestønad {
            return BarnetrygdOgHjelpestønad((omsorgsmåneder + other.omsorgsmåneder).toSet())
        }

        companion object {
            fun none(): BarnetrygdOgHjelpestønad {
                return BarnetrygdOgHjelpestønad(emptySet())
            }
        }
    }

    data class Omsorgsmåned(
        val måned: YearMonth,
        val omsorgstype: DomainOmsorgstype
    )
}

fun Set<Omsorgsmåneder.Omsorgsmåned>.måneder(): Set<YearMonth> {
    return map { it.måned }.toSet()
}