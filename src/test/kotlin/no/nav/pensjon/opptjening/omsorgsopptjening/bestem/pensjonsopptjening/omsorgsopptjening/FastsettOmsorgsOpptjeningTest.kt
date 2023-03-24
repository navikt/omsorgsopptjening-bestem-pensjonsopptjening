package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Month
import java.time.YearMonth

internal class FastsettOmsorgsOpptjeningTest {

    @Test
    fun `Given omsorgs arbeid for seven months When calling fastsettOmsorgsOpptjening Then INVILGET`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JULY),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 2005)

        val opptjening =
            FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(Utfall.INVILGET, opptjening.utfall)
    }

    @Test
    fun `Given omsorgs arbeid for less than seven months When calling fastsettOmsorgsOpptjening Then AVSLAG`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015)

        val opptjening =
            FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(Utfall.AVSLAG, opptjening.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "1984, 2000, AVSLAG",
        "1983, 2000, INVILGET",
        "1931, 2000, INVILGET",
        "1930, 2000, AVSLAG",
    )
    fun `Given person over 16 and under 70 When calling fastsettOmsorgsOpptjening Then INVILGET`(
        fodselsAr: Int,
        omsorgsAr: Int,
        expectedUtfall: Utfall
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, fodselsAr)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 1995)

        val opptjening =
            FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(expectedUtfall, opptjening.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "2000, 2001, AVSLAG",
        "2000, 2000, INVILGET",
        "2005, 2000, INVILGET",
        "2006, 2000, AVSLAG",
    )
    fun `Given seven months of omsorgs arbeid for child beneath six years When calling fastsettOmsorgsOpptjening Then INVILGET`(
        omsorgsAr: Int,
        fodselsAr: Int,
        expectedUtfall: Utfall
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1960)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, fodselsAr)

        val opptjening =
            FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(expectedUtfall, opptjening.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "2006, 2001, 2001, INVILGET",
        "2006, 2001, 2000, INVILGET",
        "2006, 2000, 2001, INVILGET",
        "2006, 2000, 2000, AVSLAG",
    )
    fun `Given seven months of omsorgs arbeid for children beneath six years When calling fastsettOmsorgsOpptjening Then INVILGET`(
        omsorgsAr: Int,
        fodselsArOmsorgsMottaker1: Int,
        fodselsArOmsorgsMottaker2: Int,
        expectedUtfall: Utfall
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf(FNR_OMSORGSMOTTAKER, FNR_OMSORGSMOTTAKER_2)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1960)
        val omsorgsmottaker1 = createPerson(FNR_OMSORGSMOTTAKER, fodselsArOmsorgsMottaker1)
        val omsorgsmottaker2 = createPerson(FNR_OMSORGSMOTTAKER_2, fodselsArOmsorgsMottaker2)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(
            omsorgsArbeidSnapshot,
            person,
            listOf(omsorgsmottaker1, omsorgsmottaker2)
        )

        assertEquals(expectedUtfall, opptjening.utfall)
    }

    @Test
    fun `Given no omsorgsmottaker When calling fastsettOmsorgsOpptjening Then AVSLAG`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2006,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2006, Month.JANUARY),
                    tom = YearMonth.of(2006, Month.JULY),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf()
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1960)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf())

        assertEquals(Utfall.AVSLAG, opptjening.utfall)
    }

    @Test
    fun `Given omsorgs arbeid for seven months When having omsorgs for more than one person Then INVILGET`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsyterFnr = FNR_OMSORGSGIVER,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JULY),
                    omsorgsyterFnr = FNR_OMSORGSGIVER,
                    omsorgsmottakere = listOf(FNR_OMSORGSMOTTAKER, FNR_OMSORGSMOTTAKER_2, FNR_OMSORGSMOTTAKER_3)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker1 = createPerson(FNR_OMSORGSMOTTAKER, 2005)
        val omsorgsmottaker2 = createPerson(FNR_OMSORGSMOTTAKER_2, 2007)
        val omsorgsmottaker3 = createPerson(FNR_OMSORGSMOTTAKER_3, 1995)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(
            omsorgsArbeidSnapshot,
            person,
            listOf(omsorgsmottaker1, omsorgsmottaker2, omsorgsmottaker3)
        )

        assertEquals(opptjening.omsorgsmottakereInvilget.size, 2)
        assertTrue(opptjening.omsorgsmottakereInvilget.containsAll(listOf(omsorgsmottaker1, omsorgsmottaker2)))
        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(Utfall.INVILGET, opptjening.utfall)
    }

    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsyterFnr: String,
        omsorgsarbeidPerioder: List<OmsorgsarbeidPeriode>
    ) =

        OmsorgsarbeidSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHashe = "xxx",
            omsorgsyter = Person(alleFnr = mutableSetOf(Fnr(fnr = omsorgsyterFnr)), fodselsAr = 1988),
            omsorgstype = Omsorgstype.BARNETRYGD,
            kilde = Kilde.BARNETRYGD,
            omsorgsarbeidSaker = listOf(
                OmsorgsarbeidSak(
                    omsorgsarbeidPerioder = omsorgsarbeidPerioder
                )
            )
        )

    private fun createOmsorgsArbeid(
        fom: YearMonth,
        tom: YearMonth,
        omsorgsyterFnr: String,
        omsorgsmottakere: List<String>
    ) = OmsorgsarbeidPeriode(
        fom = fom,
        tom = tom,
        prosent = 100,
        omsorgsyter = Person(alleFnr = mutableSetOf(Fnr(fnr = omsorgsyterFnr)), fodselsAr = 1988),
        omsorgsmottakere = omsorgsmottakere.map {
            Person(
                alleFnr = mutableSetOf(Fnr(fnr = it)),
                fodselsAr = 1988
            )
        } // TODO alder
    )


    private fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
        no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toMutableSet()
                .apply { add(Fnr(fnr = gjeldendeFnr, gjeldende = true)) },
            fodselsAr = fodselsAr
        )

    companion object {
        const val FNR_OMSORGSGIVER: String = "12345678902"
        const val FNR_OMSORGSMOTTAKER: String = "55555555555"
        const val FNR_OMSORGSMOTTAKER_2: String = "6666666666"
        const val FNR_OMSORGSMOTTAKER_3: String = "4444444444"
    }
}