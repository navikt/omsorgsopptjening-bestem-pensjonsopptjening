package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.LandstilknytningMåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class Omsorgsperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val omsorgstype: DomainOmsorgstype.Barnetrygd,
    val omsorgsmottaker: Person,
    val kilde: DomainKilde,
    val utbetalt: Int,
    val landstilknytning: Landstilknytning
) {
    val periode = Periode(fom, tom)

    fun alleMåneder(): Set<YearMonth> {
        return periode.alleMåneder().distinct().toSet()
    }

    fun utbetalingsmåneder(): Utbetalingsmåneder {
        return Utbetalingsmåneder(
            alleMåneder()
                .map { Triple(it, utbetalt, landstilknytning) }
                .mapNotNull { (mnd, utb, land) -> Utbetalingsmåned.of(mnd, utb, land) }
                .toSet()
        )
    }

    fun landstilknytningsmåneder(): Landstilknytningmåneder {
        return Landstilknytningmåneder(alleMåneder().map { LandstilknytningMåned(it, landstilknytning) }.toSet())
    }

    fun omsorgsmåneder(): Omsorgsmåneder.Barnetrygd {
        return Omsorgsmåneder.Barnetrygd(
            alleMåneder()
                .map { it to omsorgstype }
                .map { (mnd, type) -> Omsorgsmåneder.Omsorgsmåned(mnd, type) }
                .toSet()
        )
    }

    companion object {
        fun List<Omsorgsperiode>.alleMåneder(): Set<YearMonth> {
            return flatMap { it.alleMåneder() }.distinct().toSet()
        }

        fun List<Omsorgsperiode>.omsorgsmåneder(): Omsorgsmåneder.Barnetrygd {
            return map { it.omsorgsmåneder() }.reduceOrNull { acc, o -> acc.merge(o) }
                ?: Omsorgsmåneder.Barnetrygd.none()
        }

        fun List<Omsorgsperiode>.utbetalingsmåneder(): Utbetalingsmåneder {
            return map { it.utbetalingsmåneder() }.reduceOrNull { acc, o -> acc.merge(o) }
                ?: Utbetalingsmåneder.none()
        }

        fun List<Omsorgsperiode>.landstilknytningsmåneder(): Landstilknytningmåneder {
            return map { it.landstilknytningsmåneder() }.reduceOrNull { acc, o -> acc.merge(o) }
                ?: Landstilknytningmåneder.none()
        }
    }
}