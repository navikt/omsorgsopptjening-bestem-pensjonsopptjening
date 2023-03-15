package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon

class HalvtArMedOmsorgForBarnUnder6 : Vilkar<HalvtArMedOmsorgGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    avgjorelsesFunksjon = `Minst 7 moneder omsorg for barn under 6 ar`,
) {
    companion object {
        private val `Minst 7 moneder omsorg for barn under 6 ar` = fun(grunnlag: HalvtArMedOmsorgGrunnlag) =
            if (sevenMonthsOfOmsorgsarbeid(grunnlag) && beneathSixYears(grunnlag)) {
                Avgjorelse.INVILGET
            } else {
                Avgjorelse.AVSLAG
            }

        private fun sevenMonthsOfOmsorgsarbeid(grunnlag: HalvtArMedOmsorgGrunnlag): Boolean {
            return grunnlag.omsorgsArbeid.getAntallUtbetalingMoneder(grunnlag.omsorgsAr) >= 7
        }

        private fun beneathSixYears(grunnlag: HalvtArMedOmsorgGrunnlag): Boolean {
            val alder = grunnlag.omsorgsAr - grunnlag.omsorgsMottaker.fodselsAr!!
            return alder in 0..5
        }
    }
}
