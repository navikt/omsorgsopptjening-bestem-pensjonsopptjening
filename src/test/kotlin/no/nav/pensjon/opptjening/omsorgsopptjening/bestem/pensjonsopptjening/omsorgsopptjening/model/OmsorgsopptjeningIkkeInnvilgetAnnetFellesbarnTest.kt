package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnTest {
    @Test
    fun `ubestemt dersom omsorgsyter har andre barn som er innvilget en annen forelder for samme år`() {
        OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.vilkarsVurder(
            OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
                omsorgsmottaker = "barn1",
                omsorgsår = 2024,
                behandlinger = listOf(
                    OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsyter = "far",
                        omsorgsmottaker = "barn2",
                        omsorgsår = 2024,
                        erForelderTilOmsorgsmottaker = true,
                        utfall = BehandlingUtfall.Innvilget
                    )
                )
            )
        ).let {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
        }
    }

    @Test
    fun `ubestemt dersom omsorgsyter har andre barn med manuell behandling for en annen forelder for samme år`() {
        OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.vilkarsVurder(
            OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
                omsorgsmottaker = "barn1",
                omsorgsår = 2024,
                behandlinger = listOf(
                    OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsyter = "far",
                        omsorgsmottaker = "barn2",
                        omsorgsår = 2024,
                        erForelderTilOmsorgsmottaker = true,
                        utfall = BehandlingUtfall.Manuell
                    )
                )
            )
        ).let {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom omsorgsyter har andre barn som er innvilget for annen omsorgsyter som ikke er forelder til omsorgsmottaker for samme år`() {
        OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.vilkarsVurder(
            OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
                omsorgsmottaker = "barn1",
                omsorgsår = 2024,
                behandlinger = listOf(
                    OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsyter = "far",
                        omsorgsmottaker = "barn2",
                        omsorgsår = 2024,
                        erForelderTilOmsorgsmottaker = false,
                        utfall = BehandlingUtfall.Innvilget
                    )
                )
            )
        ).let {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom omsorgsyter har andre barn som er innvilget for annen omsorgsyter som ikke er forelder til omsorgsmottaker for et annet år`() {
        OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.vilkarsVurder(
            OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
                omsorgsmottaker = "barn1",
                omsorgsår = 2025,
                behandlinger = listOf(
                    OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker(
                        behandlingsId = UUID.randomUUID(),
                        omsorgsyter = "far",
                        omsorgsmottaker = "barn2",
                        omsorgsår = 2024,
                        erForelderTilOmsorgsmottaker = true,
                        utfall = BehandlingUtfall.Innvilget
                    )
                )
            )
        ).let {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom omsorgsyter ikke har andre barn som er innvilget for annen omsorgsyter`() {
        OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.vilkarsVurder(
            OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
                omsorgsmottaker = "barn1",
                omsorgsår = 2024,
                behandlinger = emptyList()
            )
        ).let {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `kaster exception hvis omsorgsmottaker er lik annen omsorgsmottaker`() {
        assertThrows<IllegalArgumentException> {
            OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.vilkarsVurder(
                OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
                    omsorgsmottaker = "barn1",
                    omsorgsår = 2024,
                    behandlinger = listOf(
                        OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker(
                            behandlingsId = UUID.randomUUID(),
                            omsorgsyter = "far",
                            omsorgsmottaker = "barn1",
                            omsorgsår = 2024,
                            erForelderTilOmsorgsmottaker = false,
                            utfall = BehandlingUtfall.Innvilget
                        )
                    )
                )
            )
        }
    }
}