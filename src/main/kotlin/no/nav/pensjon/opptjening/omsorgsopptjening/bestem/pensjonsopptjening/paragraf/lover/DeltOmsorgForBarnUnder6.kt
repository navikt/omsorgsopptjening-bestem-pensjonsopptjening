package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon


class DeltOmsorgForBarnUnder6 : Vilkar<GrunnlagOmsorgForBarnUnder6>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    utfallsFunksjon = `Minst 7 moneder omsorg for barn under 6 ar`,
) {
    companion object {
        private val `Minst 7 moneder omsorg for barn under 6 ar` = fun(grunnlag: GrunnlagOmsorgForBarnUnder6) =
            if (grunnlag.minimumOmsorgsarbeid(moneder = 7, ar = grunnlag.omsorgsAr) && grunnlag.omsorgsmottaker(alder = 0..5)) {
                Utfall.SAKSBEHANDLING
            } else if (grunnlag.minimumOmsorgsarbeid(moneder = 1, ar = grunnlag.omsorgsAr) && grunnlag.omsorgsmottaker(0..0)) {
                Utfall.SAKSBEHANDLING
            } else if (grunnlag.minimumOmsorgsarbeid(moneder = 1, ar = grunnlag.omsorgsAr + 1) && grunnlag.omsorgsmottaker(0..0)) {
                Utfall.SAKSBEHANDLING
            } else {
                Utfall.AVSLAG
            }

        private fun GrunnlagOmsorgForBarnUnder6.minimumOmsorgsarbeid(moneder: Int, ar: Int): Boolean {
            return omsorgsArbeid.getAntallUtbetalingMoneder(ar) >= moneder
        }

        private fun GrunnlagOmsorgForBarnUnder6.omsorgsmottaker(alder: IntRange): Boolean {
            return (omsorgsAr - omsorgsmottaker.fodselsAr) in alder
        }
    }
}