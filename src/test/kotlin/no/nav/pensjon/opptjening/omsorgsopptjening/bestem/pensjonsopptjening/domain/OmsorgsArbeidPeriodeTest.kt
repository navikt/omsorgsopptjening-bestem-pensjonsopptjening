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


internal class OmsorgsArbeidPeriodeTest {

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
    fun `Given fom first day of month When calling monthsOfOmsorg Then return months of omsorgsarbeid omsorgsAr`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                omsorgsAr = "2020",
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom), tom = LocalDate.parse(tom))
                )
            )
        )

        assertEquals(expectedAmountOfMoths, omsorgsArbeid.monthsWithOmsorg())
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01-31, 2020-01-31, 1",
        "2020-01-31, 2020-02-01, 2",
        "2020-01-31, 2020-03-01, 3",
        "2020-01-31, 2020-04-01, 4",
        "2020-01-31, 2020-05-01, 5",
        "2020-01-31, 2020-06-01, 6",
        "2020-01-31, 2020-07-01, 7",
        "2020-01-31, 2020-08-01, 8",
        "2020-01-31, 2020-09-01, 9",
        "2020-01-31, 2020-10-01, 10",
        "2020-01-31, 2020-11-01, 11",
        "2020-01-31, 2020-12-01, 12",
    )
    fun `Given fom last day og month When calling monthsOfOmsorg Then return months of omsorgsarbeid omsorgsAr`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                omsorgsAr = "2020",
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom), tom = LocalDate.parse(tom))
                )
            )
        )

        assertEquals(expectedAmountOfMoths, omsorgsArbeid.monthsWithOmsorg())
    }


    @ParameterizedTest
    @CsvSource(
        "2019-02-01, 2021-02-01, 12",
        "2019-12-31, 2021-01-01, 12",
        "2019-12-31, 2020-01-01, 1",
        "2020-12-31, 2021-01-01, 1",
        "2019-02-01, 2020-06-01, 6",
        "2019-12-31, 2020-06-01, 6",
        "2020-07-01, 2021-07-01, 6",
        "2020-07-31, 2021-01-01, 6",
    )
    fun `Given fom or tom overlap with omsorgsAr Then return months in omsorgsAr`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                omsorgsAr = "2020",
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom), tom = LocalDate.parse(tom))
                )
            )
        )

        assertEquals(expectedAmountOfMoths, omsorgsArbeid.monthsWithOmsorg())
    }

    @ParameterizedTest
    @CsvSource(
        "2019-01-01, 2019-12-31",
        "2021-01-01, 2021-06-01",
    )
    fun `Given fom and tom dont overlap with omsorgsAr Then return zero months`(
        fom: String,
        tom: String,
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                omsorgsAr = "2020",
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom), tom = LocalDate.parse(tom))
                )
            )
        )

        assertEquals(0, omsorgsArbeid.monthsWithOmsorg())
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01-01, 2020-12-01, 2020-01-01, 2020-12-01, 12",
        "2020-01-01, 2020-06-01, 2020-07-01, 2020-12-01, 12",
        "2019-01-01, 2020-06-01, 2020-07-01, 2022-12-01, 12",
        "2020-01-01, 2020-12-01, 2021-01-01, 2019-12-31, 12",
        "2020-01-01, 2020-06-01, 2020-08-01, 2020-12-01, 11",
        "2019-01-01, 2020-06-01, 2020-08-01, 2021-01-01, 11",
        "2019-01-01, 2019-12-31, 2021-01-01, 2019-12-31, 0",
        "2019-01-01, 2019-06-01, 2021-06-01, 2019-12-31, 0",

        )
    fun `Given given two utbetalings periodes Then count months overlaping with omsorgsAr`(
        fom1: String,
        tom1: String,
        fom2: String,
        tom2: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                omsorgsAr = "2020",
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom1), tom = LocalDate.parse(tom1)),
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom2), tom = LocalDate.parse(tom2))
                )
            )
        )

        assertEquals(expectedAmountOfMoths, omsorgsArbeid.monthsWithOmsorg())
    }

    @ParameterizedTest
    @CsvSource(
            "2020-01-01, 2020-12-01, 2020-01-01, 2020-12-01, 2020-01-01, 2020-12-01, 12",
            "2019-01-01, 2021-01-01, 2019-01-01, 2021-01-01, 2019-12-31, 2020-12-01, 12",
            "2020-01-01, 2020-03-01, 2020-04-12, 2020-06-20, 2020-10-31, 2021-12-01, 9",
            "2012-01-01, 2012-03-01, 2019-01-01, 2019-12-31, 2021-01-01, 2021-01-01, 0",


        )
    fun `Given given thre utbetalings periodes Then count months overlaping with omsorgsAr`(
        fom1: String,
        tom1: String,
        fom2: String,
        tom2: String,
        fom3: String,
        tom3: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(
                omsorgsAr = "2020",
                utbetalingsPeriode = listOf(
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom1), tom = LocalDate.parse(tom1)),
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom2), tom = LocalDate.parse(tom2)),
                    creatUtbetalingsPeriodeModel(fom = LocalDate.parse(fom3), tom = LocalDate.parse(tom3))
                )
            )
        )

        assertEquals(expectedAmountOfMoths, omsorgsArbeid.monthsWithOmsorg())
    }


    @Test
    fun `Given omsorgsarbeid without utbetalingsperioder When calling monthsOfOmsorg Then 0`() {
        val omsorgsArbeid = OmsorgsArbeidFactory.createOmsorgsArbeid(
            creatOmsorgsArbeidModel(utbetalingsPeriode = listOf(), omsorgsAr = "2020")
        )

        assertEquals(0, omsorgsArbeid.monthsWithOmsorg())
    }


    private fun creatOmsorgsArbeidModel(omsorgsAr: String, utbetalingsPeriode: List<UtbetalingsPeriodeModel>) =
        OmsorgsArbeidModel(
            omsorgsAr = omsorgsAr,
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