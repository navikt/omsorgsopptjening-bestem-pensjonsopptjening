package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LandstilknytningmånederTest {

    @Test
    fun `landstilknytning for enkeltmåned`() {
        val måneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge)
        assertThat(måneder.erNorge(januar(2023))).isFalse()
        assertThat(måneder.erNorge(januar(2024))).isTrue()
        assertThat(måneder.erNorge(desember(2024))).isTrue()
        assertThat(måneder.erNorge(desember(2025))).isFalse()
    }

    @Test
    fun `landstilknytning for flere måneder svarer true hvis fullstendig overlapp`() {
        val måneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge)
        assertThat(måneder.erNorge(år(2024).alleMåneder())).isTrue()
        assertThat(måneder.erNorge(setOf(januar(2024)))).isTrue()
        assertThat(måneder.erNorge(Periode(desember(2023), desember(2024)).alleMåneder())).isFalse()
        assertThat(måneder.erNorge(Periode(desember(2024), januar(2025)).alleMåneder())).isFalse()
    }

    @Test
    fun `kan ikke merge hvis måneder overlapper`() {
        val måneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge)
        val andreMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge)
        assertThrows<IllegalArgumentException> {
            måneder.merge(andreMåneder)
        }
    }

    @Test
    fun `kan merge hvis måneder ikke er overlappende`() {
        val måneder = Periode(januar(2024), februar(2024)).landstilknytningmåneder(Landstilknytning.Norge)
        val andreMåneder =
            Periode(mars(2024), april(2024)).landstilknytningmåneder(Landstilknytning.Eøs.NorgeSekundærland)
        val andreAndreMåneder =
            Periode(mai(2024), juni(2024)).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand)
        val merged = måneder.merge(andreMåneder).merge(andreAndreMåneder)
        assertThat(merged.forMåned(februar(2024))).isEqualTo(Landstilknytning.Norge)
        assertThat(merged.forMåned(april(2024))).isEqualTo(Landstilknytning.Eøs.NorgeSekundærland)
        assertThat(merged.forMåned(juni(2024))).isEqualTo(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand)
    }
}

fun Periode.landstilknytningmåneder(landstilknytning: Landstilknytning): Landstilknytningmåneder {
    return Landstilknytningmåneder(alleMåneder().map { LandstilknytningMåned(it, landstilknytning) }.toSet())
}