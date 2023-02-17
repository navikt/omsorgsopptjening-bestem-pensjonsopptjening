package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory.PersonFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.finnOmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.getUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.YearMonth


internal class OmsorgsArbeidsUtbetalingerTestSak {

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-01, 1",
        "2020-01, 2020-02, 2",
        "2020-01, 2020-03, 3",
        "2020-01, 2020-04, 4",
        "2020-01, 2020-05, 5",
        "2020-01, 2020-06, 6",
        "2020-01, 2020-07, 7",
        "2020-01, 2020-08, 8",
        "2020-01, 2020-09, 9",
        "2020-01, 2020-10, 10",
        "2020-01, 2020-11, 11",
        "2020-01, 2020-12, 12",
    )
    fun `Given fom first day of month When calling monthsOfOmsorg Then return months of omsorgsarbeid omsorgsAr`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsYter = FNR_1,
            omsorgsAr = 2020,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom), tom = YearMonth.parse(tom))
            )
        )


        assertEquals(
            expectedAmountOfMoths,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-01, 1",
        "2020-01, 2020-02, 2",
        "2020-01, 2020-03, 3",
        "2020-01, 2020-04, 4",
        "2020-01, 2020-05, 5",
        "2020-01, 2020-06, 6",
        "2020-01, 2020-07, 7",
        "2020-01, 2020-08, 8",
        "2020-01, 2020-09, 9",
        "2020-01, 2020-10, 10",
        "2020-01, 2020-11, 11",
        "2020-01, 2020-12, 12",
    )
    fun `Given fom last day og month When calling monthsOfOmsorg Then return months of omsorgsarbeid omsorgsAr`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsYter = FNR_1,
            omsorgsAr = 2020,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom), tom = YearMonth.parse(tom))
            )
        )


        assertEquals(
            expectedAmountOfMoths,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }


    @ParameterizedTest
    @CsvSource(
        "2019-02, 2021-02, 12",
        "2019-12, 2021-01, 12",
        "2019-12, 2020-01, 1",
        "2020-12, 2021-01, 1",
        "2019-02, 2020-06, 6",
        "2019-12, 2020-06, 6",
        "2020-07, 2021-07, 6",
        "2020-07, 2021-01, 6",
    )
    fun `Given fom or tom overlap with omsorgsAr Then return months in omsorgsAr`(
        fom: String,
        tom: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsYter = FNR_1,
            omsorgsAr = 2020,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom), tom = YearMonth.parse(tom))
            )
        )


        assertEquals(
            expectedAmountOfMoths,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }

    @ParameterizedTest
    @CsvSource(
        "2019-01, 2019-12",
        "2021-01, 2021-06",
    )
    fun `Given fom and tom dont overlap with omsorgsAr Then return zero months`(
        fom: String,
        tom: String,
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsYter = FNR_1,
            omsorgsAr = 2020,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom), tom = YearMonth.parse(tom))
            )
        )


        assertEquals(
            0,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, 12",
        "2020-01, 2020-06, 2020-07, 2020-12, 12",
        "2019-01, 2020-06, 2020-07, 2022-12, 12",
        "2020-01, 2020-12, 2021-01, 2019-12, 12",
        "2020-01, 2020-06, 2020-08, 2020-12, 11",
        "2019-01, 2020-06, 2020-08, 2021-01, 11",
        "2019-01, 2019-12, 2021-01, 2019-12, 0",
        "2019-01, 2019-06, 2021-06, 2019-12, 0",

        )
    fun `Given given two utbetalings periodes Then count months overlaping with omsorgsAr`(
        fom1: String,
        tom1: String,
        fom2: String,
        tom2: String,
        expectedAmountOfMoths: Int
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsYter = FNR_1,
            omsorgsAr = 2020,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom1), tom = YearMonth.parse(tom1)),
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom2), tom = YearMonth.parse(tom2))
            )
        )

        assertEquals(
            expectedAmountOfMoths,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, 2020-01, 2020-12, 12",
        "2019-01, 2021-01, 2019-01, 2021-01, 2019-12, 2020-12, 12",
        "2020-01, 2020-03, 2020-04, 2020-06, 2020-10, 2021-12, 9",
        "2012-01, 2012-03, 2019-01, 2019-12, 2021-01, 2021-01, 0",


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
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsYter = FNR_1,
            omsorgsAr = 2020,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom1), tom = YearMonth.parse(tom1)),
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom2), tom = YearMonth.parse(tom2)),
                creatUtbetalingsPeriode(fom = YearMonth.parse(fom3), tom = YearMonth.parse(tom3))
            )
        )

        assertEquals(
            expectedAmountOfMoths,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }


    @Test
    fun `Given omsorgsarbeid without utbetalingsperioder When calling monthsOfOmsorg Then 0`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(utbetalingsPeriode = listOf(), omsorgsAr = 2020)


        assertEquals(
            0,
            omsorgsArbeidSnapshot.finnOmsorgsArbeid(PersonFactory.createPerson(FNR_1)).getUtbetalinger(2020)
        )
    }

    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsYter: String = "1234566",
        utbetalingsPeriode: List<OmsorgsArbeidsUtbetalinger>
    ) =

        OmsorgsarbeidsSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHash = "xxx",
            omsorgsYter = Person(omsorgsYter),
            omsorgstype = Omsorgstype.BARNETRYGD,
            kilde = Kilde.BA,
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    utbetalingsPeriode.map {
                        OmsorgsArbeid(
                            omsorgsyter = Person(omsorgsYter),
                            omsorgsArbeidsUtbetalinger = it
                        )
                    }
                )
            )
        )

    private fun creatUtbetalingsPeriode(fom: YearMonth, tom: YearMonth) = OmsorgsArbeidsUtbetalinger(fom = fom, tom = tom)

    companion object {
        const val FNR_1: String = "12345678902"
    }
}