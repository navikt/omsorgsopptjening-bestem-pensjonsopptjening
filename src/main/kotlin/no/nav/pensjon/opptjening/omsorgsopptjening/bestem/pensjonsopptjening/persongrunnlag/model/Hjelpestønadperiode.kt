package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class Hjelpestønadperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val omsorgstype: DomainOmsorgstype.Hjelpestønad,
    val omsorgsmottaker: Person,
    val kilde: DomainKilde,
) {
    val periode = Periode(fom, tom)

    fun alleMåneder(): Set<YearMonth> {
        return periode.alleMåneder().distinct().toSet()
    }

    /**
     * Tell alle måneder hvor barnetrygd og hjelpestønad overlapper
     */
    fun omsorgsmåneder(omsorgsmåneder: Omsorgsmåneder.Barnetrygd): Omsorgsmåneder.Hjelpestønad {
        return Omsorgsmåneder.Hjelpestønad(
            alleMåneder().intersect(omsorgsmåneder.alle()).map { Omsorgsmåneder.Omsorgsmåned(it, omsorgstype) }.toSet()
        )
    }

    companion object {
        @JvmName("alleMndHjelp")
        fun List<Hjelpestønadperiode>.alleMåneder(): Set<YearMonth> {
            return flatMap { it.alleMåneder() }.distinct().toSet()
        }

        fun List<Hjelpestønadperiode>.omsorgsmåneder(omsorgsmåneder: Omsorgsmåneder.Barnetrygd): Omsorgsmåneder.Hjelpestønad {
            return map { it.omsorgsmåneder(omsorgsmåneder) }.reduceOrNull { acc, o -> acc.merge(o) }
                ?: Omsorgsmåneder.Hjelpestønad.none()
        }
    }
}