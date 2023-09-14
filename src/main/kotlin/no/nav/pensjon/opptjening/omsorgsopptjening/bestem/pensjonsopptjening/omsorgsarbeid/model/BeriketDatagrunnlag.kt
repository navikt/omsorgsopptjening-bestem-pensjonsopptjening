package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

/**
 * Intern representasjon av data motatt fra en [DomainKilde], beriket med nødvendige data fra tredjeparter (f.eks PDL).
 *
 * @property omsorgsSaker alle saker hvor personer [omsorgsyter] har omsorg for inngår som en omsorgsmottaker.
 * Inneholder alltid [omsorgsyter]s egen sak, i tillegg til eventuelle saker for andre omsorgsytere (f.eks mor/far,
 * verge, institusjon etc). Saker som ikke tilhører [omsorgsyter] kan inneholde omsorgsmottakere som ikke er relevant
 * for [omsorgsyter].
 */
data class BeriketDatagrunnlag(
    val omsorgsyter: Person,
    val omsorgsSaker: List<BeriketSak>,
    val innlesingId: InnlesingId,
    val correlationId: CorrelationId
) {
    val omsorgsytersSak = omsorgsSaker.single { it.omsorgsyter == omsorgsyter }
    val omsorgsytersOmsorgsmottakere = omsorgsytersSak.omsorgsmottakere()
    val omsorgsytersOmsorgsår = omsorgsytersSak.omsorgsår()
    val alleMåneder: Set<YearMonth> = omsorgsSaker.flatMap { it.måneder() }.distinct().toSet()
}

data class BeriketSak(
    val omsorgsyter: Person,
    val omsorgVedtakPerioder: List<BeriketVedtaksperiode>
) {
    fun omsorgsmottakere(): Set<Person> {
        return omsorgVedtakPerioder.map { it.omsorgsmottaker }.distinct().toSet()
    }

    fun omsorgsår(): Set<Int> {
        return måneder().map { it.year }.distinct().toSet()
    }

    fun måneder(): Set<YearMonth> {
        return perioder().flatMap { it.alleMåneder() }.distinct().toSet()
    }

    fun perioder(): Set<Periode> {
        return omsorgVedtakPerioder.map { it.periode }.distinct().toSet()
    }
}

data class BeriketVedtaksperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val omsorgstype: DomainOmsorgstype,
    val omsorgsmottaker: Person
) {
    val periode = Periode(fom, tom)

    fun alleMåneder(): Set<YearMonth> {
        return periode.alleMåneder().distinct().toSet()
    }
}

fun List<BeriketVedtaksperiode>.alleMåneder(): Set<YearMonth> {
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