package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
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
}

data class Persongrunnlag(
    val omsorgsyter: Person,
    val omsorgsperioder: List<Omsorgsperiode>
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
}

data class Omsorgsperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val omsorgstype: DomainOmsorgstype,
    val omsorgsmottaker: Person,
    val kilde: DomainKilde,
    val medlemskap: Medlemskap,
) {
    val periode = Periode(fom, tom)

    fun alleMåneder(): Set<YearMonth> {
        return periode.alleMåneder().distinct().toSet()
    }
}

fun List<Omsorgsperiode>.alleMåneder(): Set<YearMonth> {
    return flatMap { it.alleMåneder() }.distinct().toSet()
}

sealed class Omsorgsmåneder(
    måneder: Set<YearMonth>
) : Set<YearMonth> by måneder {
    class Barnetrygd(
        val måneder: Set<YearMonth>
    ) : Omsorgsmåneder(måneder)

    class Hjelpestønad(
        val måneder: Set<YearMonth>,
        val barnetrygd: Set<YearMonth>,
        val hjelpestønad: Set<YearMonth>
    ) : Omsorgsmåneder(måneder)
}