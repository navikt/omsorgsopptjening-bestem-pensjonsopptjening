package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.Og.Companion.og
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RegelTest {

    @Test
    fun bareforATeste() {
        val antallMonederMedBarnetygd = 6
        val erMedlem = false;

        val seksMonederRegel = Regel(
            regelInformasjon = RegelInformasjon(
                beskrivelse = "Bruker må ha et halvt år med barnetrygd",
                begrunnelseForInnvilgelse = "Bruker har over 6 måneder med barnetrygd",
                begrunnesleForAvslag = "Bruker har under 6 måneder med barnetrygd",
            ),
            inputVerdi = antallMonederMedBarnetygd,
            oppfyllerRegler = MINST_ET_HALVT_AR
        )

        val erFolketrygdetRegel = Regel(
            regelInformasjon = RegelInformasjon(
                beskrivelse = "Bruker må være medlem",
                begrunnelseForInnvilgelse = "Bruker er medlem",
                begrunnesleForAvslag = "Bruker er ikke medlem",
            ),
            inputVerdi = erMedlem,
            oppfyllerRegler = { it }
        )

        val ResultatOg = og(
            seksMonederRegel,
            erFolketrygdetRegel
        )

        val bareForÅPRøveResultat = og(
            eller(
                seksMonederRegel,
                seksMonederRegel,
            ),
            eller(
                seksMonederRegel,
                erFolketrygdetRegel,
            )
        )

        val resultatEller = eller(
            seksMonederRegel,
            erFolketrygdetRegel,
        )

        assertFalse(ResultatOg.brukRegel().oppFyllerRegel)
        assertTrue(resultatEller.brukRegel().oppFyllerRegel)

        println("")

    }

    companion object {
        val MINST_ET_HALVT_AR = fun(moneder: Int): Boolean = moneder >= 6
    }

}

