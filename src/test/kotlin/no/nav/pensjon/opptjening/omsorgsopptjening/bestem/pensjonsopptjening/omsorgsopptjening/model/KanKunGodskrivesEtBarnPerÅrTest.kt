package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class KanKunGodskrivesEtBarnPerÅrTest {
    @Test
    fun `innvilget hvis ingen andre behandlinger`() {
        KanKunGodskrivesEtBarnPerÅr().vilkarsVurder(
            KanKunGodskrivesEtBarnPerÅrGrunnlag(
                omsorgsmottaker = "a",
                behandlinger = emptyList()
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.EnkeltParagraf::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget hvis ingen andre behandlinger er innvilget`() {
        KanKunGodskrivesEtBarnPerÅr().vilkarsVurder(
            KanKunGodskrivesEtBarnPerÅrGrunnlag(
                omsorgsmottaker = "a",
                behandlinger = listOf(
                    AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "a",
                        erInnvilget = false
                    ),
                )
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.EnkeltParagraf::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag hvis andre behandlinger er innvilget for omsorgsmottaker`() {
        KanKunGodskrivesEtBarnPerÅr().vilkarsVurder(
            KanKunGodskrivesEtBarnPerÅrGrunnlag(
                omsorgsmottaker = "a",
                behandlinger = listOf(
                    AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "a",
                        erInnvilget = true
                    ),
                    AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "b",
                        erInnvilget = false
                    ),
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.EnkeltParagraf::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        Lovhenvisning.KAN_KUN_GODSKRIVES_ET_BARN
                    ),
                    it.lovhenvisning
                )
            }
        }
    }

    @Test
    fun `avslag hvis andre behandlinger er innvilget for andre enn omsorgsmottaker`() {
        KanKunGodskrivesEtBarnPerÅr().vilkarsVurder(
            KanKunGodskrivesEtBarnPerÅrGrunnlag(
                omsorgsmottaker = "a",
                behandlinger = listOf(
                    AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "a",
                        erInnvilget = false
                    ),
                    AndreBehandlinger(
                        behandlingsId = UUID.randomUUID(),
                        år = 2020,
                        omsorgsmottaker = "b",
                        erInnvilget = true
                    ),
                )
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.EnkeltParagraf::class.java, vurdering.utfall).also {
                kotlin.test.assertEquals(
                    setOf(
                        Lovhenvisning.KAN_KUN_GODSKRIVES_ET_BARN
                    ),
                    it.lovhenvisning
                )
            }
        }
    }
}