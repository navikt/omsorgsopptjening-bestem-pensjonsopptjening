package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Month
import java.time.YearMonth

internal class FastsettOmsorgsOpptjeningTest{
    @Test
    fun `Given omsorgs arbeid for six months When calling When calling fastsettOmsorgsOpptjening Then return opptjening invilget true`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                )
            )
        )

        val person = createPerson(FNR_1, 1990)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_1))
        assertTrue(opptjening.invilget)
    }

    @Test
    fun `Given omsorgs arbeid for less than six months When calling fastsettOmsorgsOpptjening Then return opptjening invilget false`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.MAY),
                )
            )
        )

        val person = createPerson(FNR_1, 1990)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_1))
        assertFalse(opptjening.invilget)
    }

    @ParameterizedTest
    @CsvSource(
        "2000, 2016, false",
        "2000, 2017, true",
        "2000, 2069, true",
        "2000, 2070, false",
    )
    fun `Given person over 16 and under 70 When calling fastsettOmsorgsOpptjening Then return opptjening invilget true`(
        fodselsAr: Int,
        omsorgsAr: Int,
        expectedInvilgetResult: Boolean
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsYter = FNR_1,
            utbetalingsPeriode = listOf(
                creatUtbetalingsPeriode(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JUNE),
                )
            )
        )

        val person = createPerson(FNR_1, fodselsAr)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_1))
        kotlin.test.assertEquals(expectedInvilgetResult, opptjening.invilget)
    }


    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsYter: String,
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

    private fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
        no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toMutableSet().apply { add(Fnr(fnr = gjeldendeFnr, gjeldende = true)) },
            fodselsAr = fodselsAr
        )

    companion object {
        const val FNR_1: String = "12345678902"
    }
}