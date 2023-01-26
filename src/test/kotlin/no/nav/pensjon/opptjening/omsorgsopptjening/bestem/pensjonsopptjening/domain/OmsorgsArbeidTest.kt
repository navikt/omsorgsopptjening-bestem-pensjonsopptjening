package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsMottakerModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsyterModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.UtbetalingsPeriodeModel
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

    /**

    fun `Given omsorgsarbeid for 0 monts When calling monthsOfOmsorg Then 0`() {
    val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid()
    assertEquals(0, omsorgsArbeid.monthsOfOmsorg())
    }

    fun `Given omsorgsarbeid for 12 monts When calling monthsOfOmsorg Then 0`() {
    val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid()
    assertEquals(12, omsorgsArbeid.monthsOfOmsorg())
    }
     */

    // TODO må hente ut år
    // TODO

    private fun creatOmsorgsArbeidModel() = OmsorgsArbeidModel(
        omsorgsAr = "2010",
        hash = "12345",
        omsorgsyter = OmsorgsyterModel(
            fnr = "1234566",
            utbetalingsperioder = listOf(
                UtbetalingsPeriodeModel(
                    omsorgsmottaker = OmsorgsMottakerModel("12356574353"),
                    fom = LocalDate.of(2020, Month.JANUARY, 1),
                    tom = LocalDate.of(2020, Month.JUNE, 1),
                )
            )
        )
    )
}