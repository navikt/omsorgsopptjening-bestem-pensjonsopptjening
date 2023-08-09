package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
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
    val omsorgstype: DomainOmsorgstype,
    val kjoreHash: String,
    val kilde: DomainKilde,
    val omsorgsSaker: List<BeriketSak>
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
    fun månederOmsorgFor(omsorgsmottaker: Person): Int {
        return omsorgVedtakPerioder
            .filter { it.omsorgsmottaker == omsorgsmottaker }
            .sumOf { it.periode.antallMoneder() }
    }

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
    val prosent: Int,
    val omsorgsmottaker: Person
) {
    val periode = Periode(fom, tom)
}