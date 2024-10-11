package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.september
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MedlemskapsunntakTest {

    @Test
    fun `begrenset til år er lik original hvis man begrenser til samme år som original`() {
        val original = Medlemskapsunntak(
            ikkeMedlem = setOf(
                MedlemskapsunntakPeriode(
                    fraOgMed = mars(2024),
                    tilOgMed = september(2024),
                )
            ),
            pliktigEllerFrivillig = setOf(
                MedlemskapsunntakPeriode(
                    fraOgMed = mars(2024),
                    tilOgMed = september(2024),
                )
            ),
            rådata = "whatever"
        )

        val begrenset = original.avgrensForÅr(2024)
        assertThat(begrenset).isEqualTo(original)
    }

    @Test
    fun `begrenset er tom hvis man begrenser til et år uten data`() {
        val original = Medlemskapsunntak(
            ikkeMedlem = setOf(
                MedlemskapsunntakPeriode(
                    fraOgMed = mars(2024),
                    tilOgMed = september(2024),
                )
            ),
            pliktigEllerFrivillig = emptySet(),
            rådata = "whatever"
        )

        val begrenset = original.avgrensForÅr(2023)

        assertThat(begrenset).isEqualTo(
            Medlemskapsunntak(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = emptySet(),
                rådata = "whatever"
            ),
        )
    }

    @Test
    fun `kapper perioder som strekker seg ut over angitt år til å være innefor året`() {
        val original = Medlemskapsunntak(
            ikkeMedlem = setOf(
                MedlemskapsunntakPeriode(
                    fraOgMed = mars(2023),
                    tilOgMed = september(2025),
                )
            ),
            pliktigEllerFrivillig = setOf(
                MedlemskapsunntakPeriode(
                    fraOgMed = mars(2023),
                    tilOgMed = september(2025),
                )
            ),
            rådata = "whatever"
        )

        val begrenset = original.avgrensForÅr(2024)

        assertThat(begrenset).isEqualTo(
            Medlemskapsunntak(
                ikkeMedlem = setOf(
                    MedlemskapsunntakPeriode(
                        fraOgMed = januar(2024),
                        tilOgMed = desember(2024),
                    )
                ),
                pliktigEllerFrivillig = setOf(
                    MedlemskapsunntakPeriode(
                        fraOgMed = januar(2024),
                        tilOgMed = desember(2024),
                    )
                ),
                rådata = "whatever"
            )
        )
    }
}