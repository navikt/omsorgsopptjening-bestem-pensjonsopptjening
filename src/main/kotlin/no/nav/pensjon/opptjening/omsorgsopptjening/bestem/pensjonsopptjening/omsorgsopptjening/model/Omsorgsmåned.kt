package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import java.time.YearMonth

sealed class Omsorgsmåneder {
    protected abstract val måneder: Set<YearMonth>

    fun alle(): Set<YearMonth> {
        return måneder
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

    abstract fun kvalifisererForAutomatiskBehandling(): Omsorgsmåneder
    fun erKvalifisertForAutomatiskBehandling(antallMånederRegel: AntallMånederRegel): Boolean {
        return kvalifisererForAutomatiskBehandling().antall().oppfyller(antallMånederRegel)
    }

    abstract fun kvalifisererForManuellBehandling(): Omsorgsmåneder
    fun erKvalifisertForManuellBehandling(antallMånederRegel: AntallMånederRegel): Boolean {
        return kvalifisererForManuellBehandling().antall().oppfyller(antallMånederRegel)
    }

    data class Barnetrygd(
        val omsorgsmåneder: Set<Omsorgsmåned>
    ) : Omsorgsmåneder() {
        init {
            require(omsorgsmåneder.all { it.omsorgstype.omsorgskategori() == DomainOmsorgskategori.BARNETRYGD })
        }

        override val måneder: Set<YearMonth> = omsorgsmåneder.map { it.måned }.toSortedSet()

        override fun kvalifisererForAutomatiskBehandling(): Omsorgsmåneder {
            return Barnetrygd(full())
        }

        override fun kvalifisererForManuellBehandling(): Omsorgsmåneder {
            return Barnetrygd(full() + delt())
        }

        fun merge(other: Barnetrygd): Barnetrygd {
            return Barnetrygd((omsorgsmåneder + other.omsorgsmåneder).toSet())
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

        companion object {
            fun none(): Barnetrygd {
                return Barnetrygd(emptySet())
            }
        }
    }

    data class Hjelpestønad(
        val omsorgsmåneder: Set<Omsorgsmåned>,
    ) : Omsorgsmåneder() {
        init {
            require(omsorgsmåneder.all { it.omsorgstype.omsorgskategori() == DomainOmsorgskategori.HJELPESTØNAD })
        }

        override val måneder = omsorgsmåneder.map { it.måned }.toSortedSet()

        //TODO et hull her ift full vs delt barnetrygd
        override fun kvalifisererForAutomatiskBehandling(): Omsorgsmåneder {
            return this
        }

        //TODO et hull her ift full vs delt barnetrygd
        override fun kvalifisererForManuellBehandling(): Omsorgsmåneder {
            return this
        }


        fun merge(other: Hjelpestønad): Hjelpestønad {
            return Hjelpestønad((omsorgsmåneder + other.omsorgsmåneder).toSet())
        }

        companion object {
            fun none(): Hjelpestønad {
                return Hjelpestønad(emptySet())
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