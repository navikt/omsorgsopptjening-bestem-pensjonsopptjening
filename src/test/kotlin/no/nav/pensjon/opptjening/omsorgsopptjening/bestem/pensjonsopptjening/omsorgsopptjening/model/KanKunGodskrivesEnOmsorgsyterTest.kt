package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.util.UUID

class KanKunGodskrivesEnOmsorgsyterTest {

    @Test
    fun `innvilget dersom det ikke eksisterer behandlinger for omsorgsåret`() {
        OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag(
                omsorgsår = 2020,
                fullførteBehandlinger = listOf()
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom det ikke eksisterer innvilget behandling i omsorgsåret`() {
        OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag(
                omsorgsår = 2020,
                fullførteBehandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = false,
                        omsorgsyter = "1",
                        omsorgsmottaker = "2",
                        omsorgsår = 2020
                    ),
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = false,
                        omsorgsyter = "2",
                        omsorgsmottaker = "2",
                        omsorgsår = 2020
                    ),
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = true,
                        omsorgsyter = "3",
                        omsorgsmottaker = "2",
                        omsorgsår = 2019
                    ),
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = true,
                        omsorgsyter = "3",
                        omsorgsmottaker = "2",
                        omsorgsår = 2021
                    )
                )
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom det eksisterer innvilget behandling i omsorgsåret`() {
        OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag(
                omsorgsår = 2020,
                fullførteBehandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = false,
                        omsorgsyter = "1",
                        omsorgsmottaker = "2",
                        omsorgsår = 2020
                    ),
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = true,
                        omsorgsyter = "2",
                        omsorgsmottaker = "2",
                        omsorgsår = 2020
                    )
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Første_Punktum,
                    ),
                    it.henvisninger
                )
            }
        }
    }
}