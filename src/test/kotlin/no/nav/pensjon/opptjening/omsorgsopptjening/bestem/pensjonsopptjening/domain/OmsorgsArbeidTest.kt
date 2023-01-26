package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsMottakerModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsyterModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.UtbetalingsPeriodeModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory.OmsorgsArbeidFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.Month

internal class OmsorgsArbeidTest {


    @ParameterizedTest
    @CsvSource(
            "2020-01-01, 2020-01-01, 1",
            "2020-01-01, 2020-02-01, 2",
            "2020-01-01, 2020-03-01, 3",
            "2020-01-01, 2020-04-01, 4",
            "2020-01-01, 2020-05-01, 5",
            "2020-01-01, 2020-06-01, 6",
            "2020-01-01, 2020-07-01, 7",
            "2020-01-01, 2020-08-01, 8",
            "2020-01-01, 2020-09-01, 9",
            "2020-01-01, 2020-10-01, 10",
            "2020-01-01, 2020-11-01, 11",
            "2020-01-01, 2020-12-01, 12",
    )
    fun `Given omsorgsarbeid When calling monthsOfOmsorg Then return months of omsorgsarbeid`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Long
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(
                        fom = LocalDate.parse(fom),
                        tom = LocalDate.parse(tom)
                    )
                )
            )
        )

        assertEquals(expectedAmountOfMoths, omsorgsArbeid.monthsOfOmsorg())
    }

    @Test
    fun `Given omsorgsarbeid without utbetalingsperioder When calling monthsOfOmsorg Then 0`() {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(utbetalingsPeriode = listOf())
        )

        assertEquals(0, omsorgsArbeid.monthsOfOmsorg())
    }

    // TODO Parameterized tests og flere edge caser.
    // Hva betyr fom og tom for BA

    private fun creatOmsorgsArbeidModel(
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