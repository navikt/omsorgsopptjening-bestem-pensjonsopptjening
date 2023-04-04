package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagDeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsInformasjon


class DeltOmsorgForBarnUnder6 : Vilkar<GrunnlagDeltOmsorgForBarnUnder6>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    utfallsFunksjon = `Minst 7 moneder omsorg for barn under 6 ar`,
) {
    companion object {
        private val `Minst 7 moneder omsorg for barn under 6 ar` = fun(grunnlag: GrunnlagDeltOmsorgForBarnUnder6) =
            grunnlag.run {
                when {
                    sjuMonederDeltOmsorg(ar = omsorgsAr) && alderMottaker(alder = 0..5) -> {
                        if (andreOmsorgsGivere.all { it.harInvilgetOmsorgForUrelaterBarn }) Utfall.INVILGET else Utfall.SAKSBEHANDLING
                    }

                    enMonedDeltOmsorg(ar = omsorgsAr) && alderMottaker(alder = 0..0) -> {
                        if (andreOmsorgsGivere.all { it.harInvilgetOmsorgForUrelaterBarn }) Utfall.INVILGET else Utfall.SAKSBEHANDLING
                    }

                    enMonedDeltOmsorg(ar = omsorgsAr + 1) && alderMottaker(alder = 0..0) -> {
                        if (andreOmsorgsGivere.all { it.harInvilgetOmsorgForUrelaterBarn }) Utfall.INVILGET else Utfall.SAKSBEHANDLING
                    }

                    else -> {
                        Utfall.AVSLAG
                    }
                }
            }

        private fun GrunnlagDeltOmsorgForBarnUnder6.sjuMonederDeltOmsorg(ar: Int) =
            omsorgsArbeid50Prosent.getAntallUtbetalingMoneder(ar) >= 7

        private fun GrunnlagDeltOmsorgForBarnUnder6.enMonedDeltOmsorg(ar: Int) =
            omsorgsArbeid50Prosent.getAntallUtbetalingMoneder(ar) >= 1

        private fun GrunnlagDeltOmsorgForBarnUnder6.alderMottaker(alder: IntRange) =
            (omsorgsAr - omsorgsmottaker.fodselsAr) in alder

    }
}