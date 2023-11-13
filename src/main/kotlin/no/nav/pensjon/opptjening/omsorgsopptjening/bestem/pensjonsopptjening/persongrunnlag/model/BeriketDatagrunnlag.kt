package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåned.Companion.merge
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Hjelpestønadperiode.Companion.omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode.Companion.omsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode.Companion.utbetalingsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

/**
 * Intern representasjon av data motatt fra en [DomainKilde], beriket med nødvendige data fra tredjeparter (f.eks PDL).
 *
 * @property persongrunnlag alle [Persongrunnlag] hvor personer [omsorgsyter] har omsorg for inngår som en omsorgsmottaker.
 * Inneholder alltid [omsorgsyter]s persongrunnlag, i tillegg til eventuelle persongrunnlag for andre omsorgsytere (f.eks mor/far,
 * verge, institusjon etc). Persongrunnlag som ikke tilhører [omsorgsyter] kan inneholde omsorgsmottakere som ikke er relevant
 * for [omsorgsyter].
 */
data class BeriketDatagrunnlag(
    val omsorgsyter: Person,
    val persongrunnlag: List<Persongrunnlag>,
    val innlesingId: InnlesingId,
    val correlationId: CorrelationId
) {
    val omsorgsytersPersongrunnlag = persongrunnlag.single { it.omsorgsyter == omsorgsyter }
    val omsorgsytersOmsorgsmottakere = omsorgsytersPersongrunnlag.omsorgsmottakere()
    val omsorgsytersOmsorgsår = omsorgsytersPersongrunnlag.omsorgsår()
    val alleMåneder: Set<YearMonth> = persongrunnlag.flatMap { it.måneder() }.distinct().toSet()
    fun omsorgsmånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Omsorgsmåneder.Barnetrygd> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker }.omsorgsmåneder()
        }
    }

    fun hjelpestønadMånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Omsorgsmåneder.Hjelpestønad> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.hjelpestønadperioder.filter { it.omsorgsmottaker == omsorgsmottaker }
                .omsorgsmåneder(omsorgsmånederPerOmsorgsyter(omsorgsmottaker)[pg.omsorgsyter]!!)
        }
    }

    fun utbetalingsmånederPerOmsorgsyter(omsorgsmottaker: Person): Map<Person, Utbetalingsmåneder> {
        return persongrunnlag.associate { pg ->
            pg.omsorgsyter to pg.omsorgsperioder.filter { it.omsorgsmottaker == omsorgsmottaker }.utbetalingsmåneder()
        }
    }
}

data class Persongrunnlag(
    val omsorgsyter: Person,
    val omsorgsperioder: List<Omsorgsperiode>,
    val hjelpestønadperioder: List<Hjelpestønadperiode>,
) {
    fun omsorgsmottakere(): Set<Person> {
        return omsorgsperioder.map { it.omsorgsmottaker }.distinct().toSet()
    }

    fun omsorgsår(): Set<Int> {
        return måneder().map { it.year }.distinct().toSet()
    }

    fun måneder(): Set<YearMonth> {
        return perioder().flatMap { it.alleMåneder() }.distinct().toSet()
    }

    fun perioder(): Set<Periode> {
        return omsorgsperioder.map { it.periode }.distinct().toSet()
    }

    fun utbetalingsmåneder(): Utbetalingsmåneder {
        return omsorgsperioder.map { it.utbetalingsmåneder() }.merge()
    }
}

data class Omsorgsperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val omsorgstype: DomainOmsorgstype,
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
        return Utbetalingsmåneder(periode.alleMåneder()
                                      .map { Triple(it, utbetalt, landstilknytning) }
                                      .mapNotNull { (mnd, utb, land) -> Utbetalingsmåned.of(mnd, utb, land) }
                                      .toSet()
        )
    }

    fun omsorgsmåneder(): Omsorgsmåneder.Barnetrygd {
        return Omsorgsmåneder.Barnetrygd(alleMåneder())
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
    }
}


data class Hjelpestønadperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val omsorgstype: DomainOmsorgstype,
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
        return Omsorgsmåneder.Hjelpestønad(alleMåneder().intersect(omsorgsmåneder.alleMåneder()))
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





