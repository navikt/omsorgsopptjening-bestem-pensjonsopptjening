package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrTest {
    @Test
    fun `innvilget hvis ingen andre behandlinger`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                behandlinger = emptyList()
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget hvis ingen andre behandlinger er innvilget`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                behandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "a",
                        erInnvilget = false
                    ),
                )
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag hvis andre behandlinger er innvilget for omsorgsmottaker`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                behandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "a",
                        erInnvilget = true
                    ),
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "b",
                        erInnvilget = false
                    ),
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1
                    ),
                    it.henvisninger
                )
            }
        }
    }

    @Test
    fun `avslag hvis andre behandlinger er innvilget for andre enn omsorgsmottaker`() {
        OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                omsorgsmottaker = "a",
                behandlinger = listOf(
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "a",
                        erInnvilget = false
                    ),
                    OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "b",
                        erInnvilget = true
                    ),
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1
                    ),
                    it.henvisninger
                )
            }
        }
    }
}