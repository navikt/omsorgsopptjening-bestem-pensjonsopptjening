package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import java.time.Month
import java.time.Period
import java.time.Year
import java.time.YearMonth

data class BeriketOmsorgsgrunnlag(
    val omsorgsyter: PersonMedFødselsår,
    val omsorgsAr: Int,
    val omsorgstype: DomainOmsorgstype,
    val kjoreHash: String,
    val kilde: DomainKilde,
    val omsorgsSaker: List<BeriketOmsorgSak>,
    val originaltGrunnlag: OmsorgsGrunnlag
) {
    init {
        require(Periode(YearMonth.of(omsorgsAr, Month.JANUARY), YearMonth.of(omsorgsAr, Month.DECEMBER)).alleMåneder().containsAll(omsorgsSaker.flatMap { it.omsorgVedtakPeriode }.flatMap { it.periode.alleMåneder() }.distinct())){"Grunnlag contains months outside of the omsorgsår: $omsorgsAr"}
    }
    fun hentFullOmsorgForMottaker(mottaker: PersonMedFødselsår): List<BeriketOmsorgVedtakPeriode> {
        return omsorgsytersSaker().omsorgVedtakPeriode
            .filter { it.omsorgsmottaker == mottaker }
            .filter { it.prosent == 100 }
    }

    fun antallMånederFullOmsorgForMottaker(mottaker: PersonMedFødselsår): Int {
        return hentFullOmsorgForMottaker(mottaker)
            .flatMap { it.periode.alleMåneder() }
            .toSet()
            .count()

    }

    fun omsorgsmottakere(): Set<PersonMedFødselsår> {
        return omsorgsytersSaker().omsorgVedtakPeriode.map { it.omsorgsmottaker }.toSet()
    }

    fun omsorgsytersSaker(): BeriketOmsorgSak {
        return omsorgsSaker.single { it.omsorgsyter == this.omsorgsyter }
    }
}

data class BeriketOmsorgSak(
    val omsorgsyter: PersonMedFødselsår,
    val omsorgVedtakPeriode: List<BeriketOmsorgVedtakPeriode>
)

data class BeriketOmsorgVedtakPeriode(
    val fom: YearMonth,
    val tom: YearMonth,
    val prosent: Int,
    val omsorgsmottaker: PersonMedFødselsår
) {
    val periode = Periode(fom, tom)
}

fun OmsorgsGrunnlag.berik(persondata: Set<PersonMedFødselsår>): BeriketOmsorgsgrunnlag {
    fun Set<PersonMedFødselsår>.finnPerson(fnr: String): PersonMedFødselsår {
        return single { it.fnr == fnr }
    }

    return BeriketOmsorgsgrunnlag(
        omsorgsyter = persondata.finnPerson(omsorgsyter),
        omsorgsAr = omsorgsAr,
        omsorgstype = omsorgstype.toDomain(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDomain(),
        omsorgsSaker = omsorgsSaker.map { omsorgsSak ->
            BeriketOmsorgSak(
                omsorgsyter = persondata.finnPerson(omsorgsSak.omsorgsyter),
                omsorgVedtakPeriode = omsorgsSak.omsorgVedtakPeriode.map { omsorgVedtakPeriode ->
                    BeriketOmsorgVedtakPeriode(
                        fom = omsorgVedtakPeriode.fom,
                        tom = omsorgVedtakPeriode.tom,
                        prosent = omsorgVedtakPeriode.prosent,
                        omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker)
                    )
                }

            )
        },
        originaltGrunnlag = this
    )
}
