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
            assertInstanceOf(KanKunGodskrivesEtBarnPerÅrInnvilget::class.java, it.utfall)
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
            assertInstanceOf(KanKunGodskrivesEtBarnPerÅrInnvilget::class.java, it.utfall)
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
            assertInstanceOf(KanKunGodskrivesEtBarnPerÅrAvslag::class.java, vurdering.utfall).also {
                assertEquals(
                    listOf(
                        AvslagÅrsak.ALLEREDE_GODSKREVET_BARN_FOR_ÅR
                    ),
                    it.årsaker
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
            assertInstanceOf(KanKunGodskrivesEtBarnPerÅrAvslag::class.java, vurdering.utfall).also {
                assertEquals(
                    listOf(
                        AvslagÅrsak.ALLEREDE_GODSKREVET_BARN_FOR_ÅR
                    ),
                    it.årsaker
                )
            }
        }
    }
}