package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test

class AvgjørBehandlingUtfallTest {

    private val innvilget = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
        AldersvurderingsGrunnlag(
            person = AldersvurderingsGrunnlag.AldersvurderingsPerson(
                fnr = "1",
                fødselsdato = LocalDate.of(2000, Month.JANUARY, 1)
            ),
            omsorgsAr = 2020

        )
    )

    private val avslag = innvilget.copy(utfall = VilkårsvurderingUtfall.Avslag.Vilkår(emptySet()))
    private val manuell = innvilget.copy(utfall = VilkårsvurderingUtfall.Ubestemt(emptySet()))


    @Test
    fun `en innvilget er innvilget`() {
        assertThat(AvgjørBehandlingUtfall(innvilget).utfall()).isEqualTo(BehandlingUtfall.Innvilget)
    }

    @Test
    fun `et avslag er avslag`() {
        assertThat(AvgjørBehandlingUtfall(avslag).utfall()).isEqualTo(BehandlingUtfall.Avslag)
    }

    @Test
    fun `en manuell er manuell`() {
        assertThat(AvgjørBehandlingUtfall(manuell).utfall()).isEqualTo(BehandlingUtfall.Manuell)
    }

    @Test
    fun `innvilgelse og avslag er avslag`() {
        assertThat(AvgjørBehandlingUtfall(og(innvilget, avslag)).utfall()).isEqualTo(BehandlingUtfall.Avslag)
    }

    @Test
    fun `innvilgelse og manuell er manuell`() {
        assertThat(AvgjørBehandlingUtfall(og(innvilget, manuell)).utfall()).isEqualTo(BehandlingUtfall.Manuell)
    }

    @Test
    fun `avslag og manuell er avslag`() {
        assertThat(AvgjørBehandlingUtfall(og(avslag, manuell)).utfall()).isEqualTo(BehandlingUtfall.Avslag)
    }

    @Test
    fun `innvilgelse, avslag og manuell er avslag`() {
        assertThat(AvgjørBehandlingUtfall(og(innvilget, avslag, manuell)).utfall()).isEqualTo(BehandlingUtfall.Avslag)
    }
}