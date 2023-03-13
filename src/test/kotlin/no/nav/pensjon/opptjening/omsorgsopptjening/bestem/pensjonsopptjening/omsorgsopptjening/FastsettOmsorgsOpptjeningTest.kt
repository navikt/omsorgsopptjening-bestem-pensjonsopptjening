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
            omsorgsYter = FNR_1,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JULY),
                    omsorgsYter = FNR_1,
                )
            )
        )

        val person = createPerson(FNR_1, 1990)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_1))
        assertEquals(Avgjorelse.INVILGET, opptjening.invilget)
    }

    @Test
    fun `Given omsorgs arbeid for less than seven months When calling fastsettOmsorgsOpptjening Then AVSLAG`() {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsYter = FNR_1,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                    omsorgsYter = FNR_1,
                )
            )
        )

        val person = createPerson(FNR_1, 1990)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_1))
        assertEquals(Avgjorelse.AVSLAG, opptjening.invilget)
    }

    @ParameterizedTest
    @CsvSource(
        "2000, 2016, AVSLAG",
        "2000, 2017, INVILGET",
        "2000, 2069, INVILGET",
        "2000, 2070, AVSLAG",
    )
    fun `Given person over 16 and under 70 When calling fastsettOmsorgsOpptjening Then INVILGET`(
        fodselsAr: Int,
        omsorgsAr: Int,
        expectedAvgjorelse: Avgjorelse
    ) {
        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsYter = FNR_1,
            omsorgsArbeid = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsYter = FNR_1,
                )
            )
        )

        val person = createPerson(FNR_1, fodselsAr)

        val opptjening = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSnapshot, person)

        assertTrue(opptjening.person identifiseresAv Fnr(fnr = FNR_1))
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

    private fun createOmsorgsArbeid(fom: YearMonth, tom: YearMonth, omsorgsYter:String) = OmsorgsArbeid(
        fom = fom,
        tom = tom,
        omsorgsyter = Person(omsorgsYter),
        omsorgsmottaker = listOf()
    )

    private fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
        no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toMutableSet().apply { add(Fnr(fnr = gjeldendeFnr, gjeldende = true)) },
            fodselsAr = fodselsAr
        )

    companion object {
        const val FNR_1: String = "12345678902"
    }
}