package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsArbeidsUtbetalingerOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar

class HalvtArMedOmsorg : Vilkar<OmsorgsArbeidsUtbetalingerOgOmsorgsAr>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    kalkulerAvgjorelse = `Minst 6 moneder med omsorg`,
) {
    companion object {
        private val `Minst 6 moneder med omsorg` = fun(input: OmsorgsArbeidsUtbetalingerOgOmsorgsAr) =
            input.omsorgsArbeidsUtbetalinger.getAntallUtbetalingMoneder(input.omsorgsAr) >= 6
    }
}




