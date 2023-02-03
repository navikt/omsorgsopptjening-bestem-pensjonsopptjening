package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar.RegelInformasjon

class HalvtArMedOmsorgForBarn : Vilkar<Int>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Medlemmet har minst halve året hatt den daglige omsorgen for et barn",
        begrunnesleForAvslag = "Medlemmet har ikke et halve år med daglig omsorgen for et barn",
        begrunnelseForInnvilgelse = "Medlemmet har et halve år med daglig omsorgen for et barn",
    ),
    oppfyllerRegler = `Minst 6 måneder med omsorg`,
) {
    companion object {
        val `Minst 6 måneder med omsorg` = fun(moneder: Int) = moneder >= 6
    }
}



