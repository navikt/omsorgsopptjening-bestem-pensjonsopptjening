package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class KanKunGodskrivesEnOmsorgsyterTest {

    @Test
    fun `innvilget dersom det ikke eksisterer behandlinger`() {
        OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag(
                emptyList()
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom det ikke eksisterer innvilget behandling`() {
        OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag(
                listOf(
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = false
                    ),
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = false
                    )
                )
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom det eksisterer innvilget behandling`() {
        OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.vilkarsVurder(
            OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag(
                listOf(
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = false
                    ),
                    OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
                        behandlingsId = UUID.randomUUID(),
                        erInnvilget = true
                    )
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        Forskrift.FOR_OMSORGSPOENG_K3_P4_L1_pkt1
                    ),
                    it.henvisninger
                )
            }
        }
    }
}