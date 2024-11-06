package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.YearMonth

data class Ytelsegrunnlag(
    val ytelser: Set<Ytelseinformasjon>
) {
    fun avgrensForÅr(år: Int): Ytelsegrunnlag {
        return Ytelsegrunnlag(ytelser.map { it.avgrensForÅr(år) }.toSet())
    }

    fun ytelsesmåneder(): Ytelsemåneder {
        return Ytelsemåneder(ytelser.flatMap { it.perioder }.flatMap { it.periode.alleMåneder() }.toSet())
    }
}

data class Ytelseinformasjon(
    val perioder: Set<YtelsePeriode>,
    val rådata: String,
) {
    fun avgrensForÅr(år: Int): Ytelseinformasjon {
        return Ytelseinformasjon(
            perioder = perioder.filter { it.periode.overlapper(år) }
                .map { ytelsePeriode ->
                    ytelsePeriode.periode.overlappendeMåneder(år)
                        .let {
                            YtelsePeriode(
                                fom = it.min(),
                                tom = it.max(),
                                type = ytelsePeriode.type
                            )
                        }
                }
                .toSet(),
            rådata = rådata
        )
    }
}

data class YtelsePeriode(
    val fom: YearMonth,
    val tom: YearMonth,
    val type: YtelseType,
) {
    val periode: Periode = Periode(fom, tom)
}

enum class YtelseType {
    ALDERSPENSJON,
    UFØRETRYGD,
}