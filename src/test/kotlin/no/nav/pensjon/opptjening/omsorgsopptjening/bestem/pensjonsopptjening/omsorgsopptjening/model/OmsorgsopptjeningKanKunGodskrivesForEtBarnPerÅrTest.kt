package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.util.UUID

class OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrTest {
    @Test
    fun `innvilget hvis ingen andre behandlinger i omsorgsåret`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                omsorgsår = 2020,
                behandlinger = emptyList(),
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget hvis ingen andre behandlinger er innvilget for omsorgsår`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                omsorgsår = 2020,
                behandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2020,
                        omsorgsmottaker = "a",
                        omsorgsyter = "b",
                        erInnvilget = false
                    ),
                ),
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag hvis andre behandlinger er innvilget for omsorgsmottaker i omsorgsår`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                omsorgsår = 2020,
                behandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2020,
                        omsorgsyter = "a",
                        omsorgsmottaker = "b",
                        erInnvilget = true
                    ),
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2020,
                        omsorgsyter = "a",
                        omsorgsmottaker = "c",
                        erInnvilget = false
                    ),
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2019,
                        omsorgsyter = "a",
                        omsorgsmottaker = "b",
                        erInnvilget = true
                    ),
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2021,
                        omsorgsyter = "a",
                        omsorgsmottaker = "b",
                        erInnvilget = true
                    ),
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Innledning
                    ),
                    it.henvisninger
                )
            }
        }
    }

    @Test
    fun `avslag hvis andre behandlinger er innvilget for andre enn omsorgsmottaker i omsorgsår`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                omsorgsår = 2020,
                behandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2020,
                        omsorgsyter = "a",
                        omsorgsmottaker = "b",
                        erInnvilget = false
                    ),
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsÅr = 2020,
                        omsorgsyter = "a",
                        omsorgsmottaker = "c",
                        erInnvilget = true
                    ),
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Innledning
                    ),
                    it.henvisninger
                )
            }
        }
    }
}