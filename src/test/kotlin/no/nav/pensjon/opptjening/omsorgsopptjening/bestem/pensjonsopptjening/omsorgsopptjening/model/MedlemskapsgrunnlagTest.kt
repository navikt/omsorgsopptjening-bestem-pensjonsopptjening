package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MedlemskapsgrunnlagTest {

    @Test
    fun `begrenset til år er lik original hvis man begrenser til samme år som original`() {
        val original = Medlemskapsgrunnlag(
            unntaksperioder = listOf(
                Medlemskapsgrunnlag.Unntaksperiode(
                    fraOgMed = mars(2024),
                    tilOgMed = september(2024),
                )
            ), rådata = "whatever"
        )

        val begrenset = original.avgrensForÅr(2024)
        assertThat(begrenset).isEqualTo(original)
    }

    @Test
    fun `begrenset er tom hvis man begrenser til et år uten data`() {
        val original = Medlemskapsgrunnlag(
            unntaksperioder = listOf(
                Medlemskapsgrunnlag.Unntaksperiode(
                    fraOgMed = mars(2024),
                    tilOgMed = september(2024),
                )
            ), rådata = "whatever"
        )

        val begrenset = original.avgrensForÅr(2023)

        assertThat(begrenset).isEqualTo(
            Medlemskapsgrunnlag(
                unntaksperioder = emptyList(),
                rådata = "whatever"
            ),
        )
    }

    @Test
    fun `kapper perioder som strekker seg ut over angitt år til å være innefor året`() {
        val original = Medlemskapsgrunnlag(
            unntaksperioder = listOf(
                Medlemskapsgrunnlag.Unntaksperiode(
                    fraOgMed = mars(2023),
                    tilOgMed = september(2025),
                )
            ), rådata = "whatever"
        )

        val begrenset = original.avgrensForÅr(2024)

        assertThat(begrenset).isEqualTo(
            Medlemskapsgrunnlag(
                unntaksperioder = listOf(
                    Medlemskapsgrunnlag.Unntaksperiode(
                        fraOgMed = januar(2024),
                        tilOgMed = desember(2024),
                    )
                ), rådata = "whatever"
            )
        )
    }
}