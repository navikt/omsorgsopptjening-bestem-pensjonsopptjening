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
            when {
                grunnlag.sjuMonederDeltOmsorgsArbeid(ar = grunnlag.omsorgsAr) && grunnlag.alderMottaker(0..5) -> {
                    if (grunnlag.andreOmsorgsGivereHarInvilgetOmsorgsOpptjening()) Utfall.INVILGET else Utfall.SAKSBEHANDLING
                }

                grunnlag.enMonedDeltOmsorgsArbeid(ar = grunnlag.omsorgsAr) && grunnlag.alderMottaker(0..0) -> {
                    if (grunnlag.andreOmsorgsGivereHarInvilgetOmsorgsOpptjening()) Utfall.INVILGET else Utfall.SAKSBEHANDLING
                }

                grunnlag.enMonedDeltOmsorgsArbeid(ar = grunnlag.omsorgsAr + 1) && grunnlag.alderMottaker(0..0) -> {
                    if (grunnlag.andreOmsorgsGivereHarInvilgetOmsorgsOpptjening()) Utfall.INVILGET else Utfall.SAKSBEHANDLING
                }

                else -> {
                    Utfall.AVSLAG
                }
            }

        private fun GrunnlagDeltOmsorgForBarnUnder6.andreOmsorgsGivereHarInvilgetOmsorgsOpptjening() =
            andreOmsorgsGivere.all { it.harInvilgetOmsorgForUrelaterBarn }

        private fun GrunnlagDeltOmsorgForBarnUnder6.sjuMonederDeltOmsorgsArbeid(ar: Int) =
            omsorgsArbeid50Prosent.getAntallUtbetalingMoneder(ar) >= 7

        private fun GrunnlagDeltOmsorgForBarnUnder6.enMonedDeltOmsorgsArbeid(ar: Int) =
            omsorgsArbeid50Prosent.getAntallUtbetalingMoneder(ar) >= 1

        private fun GrunnlagDeltOmsorgForBarnUnder6.alderMottaker(alder: IntRange) =
            (omsorgsAr - omsorgsmottaker.fodselsAr) in alder

    }
}