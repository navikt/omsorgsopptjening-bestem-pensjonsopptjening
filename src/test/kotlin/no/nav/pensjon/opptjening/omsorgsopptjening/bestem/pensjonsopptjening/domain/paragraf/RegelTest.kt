package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.Og.Companion.og
import org.junit.jupiter.api.Test

internal class RegelTest {

    @Test
    fun bareforATeste() {
        val antallMonederMedBarnetygd = 6

        val seksMonederRegel = Regel(
            regelInformasjon = RegelInformasjon(
                beskrivelse = "Bruker må ha et halvt år med barnetrygd",
                begrunnelseForInnvilgelse = "Bruker har over 6 måneder med barnetrygd",
                begrunnesleForAvslag = "Bruker har under 6 måneder med barnetrygd",
            ),
            inputVerdi = antallMonederMedBarnetygd,
            oppfyllerRegler = MINST_ET_HALVT_AR
        )

        val resultat = og(
            seksMonederRegel,
            seksMonederRegel
        )

        println("")

    }

    companion object {
        val MINST_ET_HALVT_AR = fun(moneder: Int): Boolean = moneder >= 6
    }

}

