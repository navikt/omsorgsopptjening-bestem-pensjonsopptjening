package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class AggregertBehandlingsutfallTest {

    private val innvilget = BehandlingUtfall.Innvilget
    private val avslag = BehandlingUtfall.Avslag
    private val manuell = BehandlingUtfall.Manuell

    @Test
    fun `en innvilget er innvilget`() {
        assertThat(AggregertBehandlingsutfall(listOf(innvilget)).utfall()).isEqualTo(AggregertBehandlingUtfall.Innvilget)
    }

    @Test
    fun `et avslag er avslag`() {
        assertThat(AggregertBehandlingsutfall(listOf(avslag)).utfall()).isEqualTo(AggregertBehandlingUtfall.Avslag)
    }

    @Test
    fun `et manuell er manuell`() {
        assertThat(AggregertBehandlingsutfall(listOf(manuell)).utfall()).isEqualTo(AggregertBehandlingUtfall.Manuell)
    }

    @Test
    fun `innvilget og avslag er innvilget`() {
        assertThat(
            AggregertBehandlingsutfall(
                listOf(
                    innvilget,
                    avslag
                )
            ).utfall()
        ).isEqualTo(AggregertBehandlingUtfall.Innvilget)
    }

    @Test
    fun `innvilget og manuell er innvilget`() {
        assertThat(
            AggregertBehandlingsutfall(
                listOf(
                    innvilget,
                    manuell
                )
            ).utfall()
        ).isEqualTo(AggregertBehandlingUtfall.Innvilget)
    }

    @Test
    fun `manuell og avslag er manuell`() {
        assertThat(
            AggregertBehandlingsutfall(
                listOf(
                    manuell,
                    avslag
                )
            ).utfall()
        ).isEqualTo(AggregertBehandlingUtfall.Manuell)
    }

    @Test
    fun `innvilget, manuell og avslag er innvilget`() {
        assertThat(AggregertBehandlingsutfall(listOf(innvilget, manuell, avslag)).utfall()).isEqualTo(
            AggregertBehandlingUtfall.Innvilget
        )
    }
}