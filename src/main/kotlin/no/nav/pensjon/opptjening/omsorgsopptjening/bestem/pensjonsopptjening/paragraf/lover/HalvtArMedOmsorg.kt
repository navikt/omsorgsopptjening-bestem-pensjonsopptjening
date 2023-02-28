package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.getUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsArbeidsUtbetalingerOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.RegelInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar

class HalvtArMedOmsorg : Vilkar<OmsorgsArbeidsUtbetalingerOgOmsorgsAr>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    oppfyllerRegler = `Minst 6 måneder med omsorg`,
) {
    companion object {
        val `Minst 6 måneder med omsorg` = fun(input: OmsorgsArbeidsUtbetalingerOgOmsorgsAr) =
            input.omsorgsArbeidsUtbetalinger.getUtbetalinger(input.omsorgsAr) >= 6
    }
}




