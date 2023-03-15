package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import org.junit.jupiter.api.Assertions.*
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
            omsorgsYter = FNR_OMSORGSGIVER,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JULY),
                    omsorgsYter = FNR_OMSORGSGIVER,
                    omsorgsMottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 2005)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(Avgjorelse.INVILGET, opptjening.invilget)
    }

    @Test
    fun `Given omsorgs arbeid for less than seven months When calling fastsettOmsorgsOpptjening Then AVSLAG`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsYter = FNR_OMSORGSGIVER,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                    omsorgsYter = FNR_OMSORGSGIVER,
                    omsorgsMottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(Avgjorelse.AVSLAG, opptjening.invilget)
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
        expectedAvgjorelse: Avgjorelse
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsYter = FNR_OMSORGSGIVER,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsYter = FNR_OMSORGSGIVER,
                    omsorgsMottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, fodselsAr)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 1995)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(expectedAvgjorelse, opptjening.invilget)
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
        expectedAvgjorelse: Avgjorelse
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsYter = FNR_OMSORGSGIVER,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsYter = FNR_OMSORGSGIVER,
                    omsorgsMottakere = listOf(FNR_OMSORGSMOTTAKER)
                )
            )
        )

        val person = createPerson(FNR_OMSORGSGIVER, 1960)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, fodselsAr)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person, listOf(omsorgsmottaker))

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_OMSORGSGIVER))
        assertEquals(expectedAvgjorelse, opptjening.invilget)
    }


    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsYter: String,
        omsorgsArbeid: List<OmsorgsArbeid>
    ) =

        OmsorgsarbeidsSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHash = "xxx",
            omsorgsYter = Person(omsorgsYter),
            omsorgstype = Omsorgstype.BARNETRYGD,
            kilde = Kilde.BA,
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(
                    omsorgsarbedUtfort = omsorgsArbeid
                )
            )
        )

    private fun createOmsorgsArbeid(fom: YearMonth, tom: YearMonth, omsorgsYter: String, omsorgsMottakere: List<String>) = OmsorgsArbeid(
        fom = fom,
        tom = tom,
        omsorgsyter = Person(omsorgsYter),
        omsorgsmottaker = omsorgsMottakere.map { Person(it) }
    )


    private fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
        no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toMutableSet().apply { add(Fnr(fnr = gjeldendeFnr, gjeldende = true)) },
            fodselsAr = fodselsAr
        )

    companion object {
        const val FNR_OMSORGSGIVER: String = "12345678902"
        const val FNR_OMSORGSMOTTAKER: String = "55555555555"
    }
}