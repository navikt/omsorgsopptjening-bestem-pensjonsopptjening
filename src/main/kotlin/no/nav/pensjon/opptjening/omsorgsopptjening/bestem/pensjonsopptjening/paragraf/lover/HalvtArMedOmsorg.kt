package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon

class HalvtArMedOmsorg : Vilkar<HalvtArMedOmsorgGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    avgjorelsesFunksjon = `Minst 7 moneder med omsorg`,
) {
    companion object {
        private val `Minst 7 moneder med omsorg` = fun(input: HalvtArMedOmsorgGrunnlag) =
            if (input.omsorgsArbeid.getAntallUtbetalingMoneder(input.omsorgsAr) >= 7) {
                Avgjorelse.INVILGET
            } else {
                Avgjorelse.AVSLAG
            }
    }
}