package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.PersonMedFødselsår
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class BeriketDatagrunnlag(
    val omsorgsyter: PersonMedFødselsår,
    val omsorgstype: DomainOmsorgstype,
    val kjoreHash: String,
    val kilde: DomainKilde,
    val omsorgsSaker: List<BeriketSak>,
    val originaltGrunnlag: String
) {
    fun omsorgsmottakere(): Set<PersonMedFødselsår> {
        return omsorgsytersSaker().omsorgVedtakPeriode.map { it.omsorgsmottaker }.toSet()
    }

    fun omsorgsytersSaker(): BeriketSak {
        return omsorgsSaker.single { it.omsorgsyter == this.omsorgsyter }
    }
}

data class BeriketSak(
    val omsorgsyter: PersonMedFødselsår,
    val omsorgVedtakPeriode: List<BeriketVedtaksperiode>
){
    fun antallMånederOmsorgFor(omsorgsmottaker: PersonMedFødselsår): Pair<PersonMedFødselsår, Int> {
        return omsorgsyter to omsorgVedtakPeriode.filter { it.omsorgsmottaker == omsorgsmottaker }.sumOf { it.periode.antallMoneder() }
    }
}

data class BeriketVedtaksperiode(
    val fom: YearMonth,
    val tom: YearMonth,
    val prosent: Int,
    val omsorgsmottaker: PersonMedFødselsår
) {
    val periode = Periode(fom, tom)
}