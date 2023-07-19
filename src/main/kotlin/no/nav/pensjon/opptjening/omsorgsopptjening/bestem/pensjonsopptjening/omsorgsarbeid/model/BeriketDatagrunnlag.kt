package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class BeriketDatagrunnlag(
    val omsorgsyter: Person,
    val omsorgstype: DomainOmsorgstype,
    val kjoreHash: String,
    val kilde: DomainKilde,
    val omsorgsSaker: List<BeriketSak>,
    val originaltGrunnlag: String
) {
    fun omsorgsmottakere(): Set<Person> {
        return omsorgsytersSaker().omsorgVedtakPerioder.map { it.omsorgsmottaker }.toSet()
    }

    fun omsorgsytersSaker(): BeriketSak {
        return omsorgsSaker.single { it.omsorgsyter == this.omsorgsyter }
    }
}

data class BeriketSak(
    val omsorgsyter: Person,
    val omsorgVedtakPerioder: List<BeriketVedtaksperiode>
) {
    fun antallMånederOmsorgFor(omsorgsmottaker: Person): Pair<Person, Int> {
        return omsorgsyter to omsorgVedtakPerioder.filter { it.omsorgsmottaker == omsorgsmottaker }
            .sumOf { it.periode.antallMoneder() }
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