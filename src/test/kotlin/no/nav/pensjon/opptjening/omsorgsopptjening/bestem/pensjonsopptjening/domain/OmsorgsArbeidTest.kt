package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsMottakerModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsyterModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.UtbetalingsPeriodeModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory.OmsorgsArbeidFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class OmsorgsArbeidTest {


    @Test
    fun `Given omsorgsarbeid for 6 monts When calling monthsOfOmsorg Then six`() {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(creatOmsorgsArbeidModel())

        assertEquals(6, omsorgsArbeid.monthsOfOmsorg())
    }

    @Test
    fun `Given omsorgsarbeid without utbetalingsperioder When calling monthsOfOmsorg Then 0`() {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(utbetalingsPeriode = listOf())
        )

        assertEquals(0, omsorgsArbeid.monthsOfOmsorg())
    }

    @Test
    fun `Given omsorgsarbeid for 12 monts When calling monthsOfOmsorg Then 12`() {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(utbetalingsPeriode = listOf(
                creatUtbetalingsPeriodeModel(
                    fom = LocalDate.of(2020, Month.JANUARY, 1),
                    tom = LocalDate.of(2020, Month.DECEMBER, 1)
                )
            ))
        )

        assertEquals(12, omsorgsArbeid.monthsOfOmsorg())
    }


    // TODO Parameterized tests og flere edge caser.
    // Hva betyr fom og tom for BA

    private fun creatOmsorgsArbeidModel(
        fom: LocalDate = LocalDate.of(2020, Month.JANUARY, 1),
        tom: LocalDate = LocalDate.of(2020, Month.JUNE, 1),
        utbetalingsPeriode: List<UtbetalingsPeriodeModel> = listOf(creatUtbetalingsPeriodeModel())

    ) = OmsorgsArbeidModel(
        omsorgsAr = "2010",
        hash = "12345",
        omsorgsyter = OmsorgsyterModel(
            fnr = "1234566",
            utbetalingsperioder = utbetalingsPeriode
        )
    )

    private fun creatUtbetalingsPeriodeModel(
        fom: LocalDate = LocalDate.of(2020, Month.JANUARY, 1),
        tom: LocalDate = LocalDate.of(2020, Month.JUNE, 1)
    ) = UtbetalingsPeriodeModel(
        omsorgsmottaker = OmsorgsMottakerModel("12356574353"),
        fom = fom,
        tom = tom,
    )
}