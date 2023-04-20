package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.getAntallUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.YearMonth

internal class OmsorgsArbeidsUtbetalingerKtTest {

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
    fun `Given fom first day of month When calling getAntallUtbetalingMoneder Then return months of omsorgsarbeid omsorgsAr`(
        fom: YearMonth,
        tom: YearMonth,
        expectedAmountOfMoths: Int
    ) {
        val perioder = listOf(
            OmsorgVedtakPeriode(fom = fom, tom = tom, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL)
        )

        assertEquals(expectedAmountOfMoths, perioder.getAntallUtbetalingMoneder(2020))
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
    fun `Given fom last day og month When calling getAntallUtbetalingMoneder Then return months of omsorgsarbeid omsorgsAr`(
        fom: YearMonth,
        tom: YearMonth,
        expectedAmountOfMoths: Int
    ) {
        val perioder = listOf(
            OmsorgVedtakPeriode(fom = fom, tom = tom, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL)
        )

        assertEquals(expectedAmountOfMoths, perioder.getAntallUtbetalingMoneder(2020))
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
        fom: YearMonth,
        tom: YearMonth,
        expectedAmountOfMoths: Int
    ) {
        val perioder = listOf(
            OmsorgVedtakPeriode(fom = fom, tom = tom, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL)
        )

        assertEquals(expectedAmountOfMoths, perioder.getAntallUtbetalingMoneder(2020))
    }

    @ParameterizedTest
    @CsvSource(
        "2019-01, 2019-12",
        "2021-01, 2021-06",
    )
    fun `Given fom and tom dont overlap with omsorgsAr Then return zero months`(
        fom: YearMonth,
        tom: YearMonth,
    ) {
        val perioder = listOf(
            OmsorgVedtakPeriode(fom = fom, tom = tom, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL)
        )

        assertEquals(0, perioder.getAntallUtbetalingMoneder(2020))
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
        "2020-01, 2020-01, 2020-03, 2020-04, 3",
    )
    fun `Given given two omsorgsArbeidsUtbetalinger Then count months overlapping with omsorgsAr`(
        fom1: YearMonth,
        tom1: YearMonth,
        fom2: YearMonth,
        tom2: YearMonth,
        expectedAmountOfMoths: Int
    ) {
        val perioder = listOf(
            OmsorgVedtakPeriode(fom = fom1, tom = tom1, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL),
            OmsorgVedtakPeriode(fom = fom2, tom = tom2, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL)
        )

        assertEquals(expectedAmountOfMoths, perioder.getAntallUtbetalingMoneder(2020))
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, 2020-01, 2020-12, 12",
        "2019-01, 2021-01, 2019-01, 2021-01, 2019-12, 2020-12, 12",
        "2020-01, 2020-03, 2020-04, 2020-06, 2020-10, 2021-12, 9",
        "2012-01, 2012-03, 2019-01, 2019-12, 2021-01, 2021-01, 0",
    )
    fun `Given given three utbetalings periodes Then count months overlapping with omsorgsAr`(
        fom1: YearMonth,
        tom1: YearMonth,
        fom2: YearMonth,
        tom2: YearMonth,
        fom3: YearMonth,
        tom3: YearMonth,
        expectedAmountOfMoths: Int
    ) {
        val perioder = listOf(
            OmsorgVedtakPeriode(fom = fom1, tom = tom1, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL),
            OmsorgVedtakPeriode(fom = fom2, tom = tom2, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL),
            OmsorgVedtakPeriode(fom = fom3, tom = tom3, prosent = 100, omsorgsytere = listOf(person), omsorgsmottakere = listOf(), landstilknytning = Landstilknytning.NASJONAL)
        )

        assertEquals(expectedAmountOfMoths, perioder.getAntallUtbetalingMoneder(2020))
    }


    @Test
    fun `Given zero omsorgsArbeidsUtbetalinger When calling getAntallUtbetalingMoneder Then 0`() {
        assertEquals(0, listOf<OmsorgVedtakPeriode>().getAntallUtbetalingMoneder(2020))
    }

    companion object {
        private val person = Person(alleFnr = mutableSetOf(Fnr(fnr = "12345", gjeldende = true)), fodselsAr = 1988)
    }
}