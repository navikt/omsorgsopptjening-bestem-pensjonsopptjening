package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar.Og.Companion.og
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class VilkarInstanseTest {

    @ParameterizedTest
    @CsvSource(
        "6, true, true",
        "6, false, false",
        "5, true, false",
        "5, false, false"
    )
    fun `Given an AND rule when evaluating the rule then return true only if all rules are true`(
        moneder: Int, erMedlem:Boolean, forventetUtfall:Boolean
    ) {
        val ogRegel = og(
            minstSeksMonederRegel(moneder = moneder),
            erFolketrygdetRegel(erMedlem = erMedlem)
        )

        assertEquals(forventetUtfall, ogRegel.utførVilkarsVurdering().oppFyllerRegel)
    }

    @ParameterizedTest
    @CsvSource(
        "6, true, true",
        "6, false, true",
        "5, true, true",
        "5, false, false"
    )
    fun `Given an OR rule when evaluating the rule then return true if one rule is true`(
        moneder: Int, erMedlem:Boolean, forventetUtfall:Boolean
    ) {
        val ellerRegel = eller(
            minstSeksMonederRegel(moneder = moneder),
            erFolketrygdetRegel(erMedlem = erMedlem)
        )

        assertEquals(forventetUtfall, ellerRegel.utførVilkarsVurdering().oppFyllerRegel)
    }

    fun minstSeksMonederRegel(moneder:Int ) = VilkarsVurdering(
        vilkarsInformasjon = VilkarsInformasjon(
            beskrivelse = "Bruker må ha et halvt år med barnetrygd",
            begrunnelseForInnvilgelse = "Bruker har over 6 måneder med barnetrygd",
            begrunnesleForAvslag = "Bruker har under 6 måneder med barnetrygd",
        ),
        oppfyllerRegler = MINST_ET_HALVT_AR,
        inputVerdi = moneder
    )

    fun erFolketrygdetRegel(erMedlem:Boolean ) = VilkarsVurdering(
        vilkarsInformasjon = VilkarsInformasjon(
            beskrivelse = "Bruker må være medlem",
            begrunnelseForInnvilgelse = "Bruker er medlem",
            begrunnesleForAvslag = "Bruker er ikke medlem",
        ),
        oppfyllerRegler = MÅ_VÆRE_FOLKETRYGDET,
        inputVerdi = erMedlem
    )


    companion object {
        val MINST_ET_HALVT_AR = fun(moneder: Int) = moneder >= 6
        val MÅ_VÆRE_FOLKETRYGDET = fun(erFolketrygdet: Boolean) = erFolketrygdet
    }

}

