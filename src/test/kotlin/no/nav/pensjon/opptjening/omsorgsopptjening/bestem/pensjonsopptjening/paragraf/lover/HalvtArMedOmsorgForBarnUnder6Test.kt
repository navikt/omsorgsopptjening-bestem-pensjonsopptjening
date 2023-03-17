package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Month
import java.time.YearMonth

internal class HalvtArMedOmsorgForBarnUnder6Test {

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-01, AVSLAG",
        "2020-01, 2020-02, AVSLAG",
        "2020-01, 2020-03, AVSLAG",
        "2020-01, 2020-04, AVSLAG",
        "2020-01, 2020-05, AVSLAG",
        "2020-01, 2020-06, AVSLAG",
        "2020-01, 2020-07, INVILGET",
        "2020-01, 2020-08, INVILGET",
        "2020-01, 2020-09, INVILGET",
        "2020-01, 2020-10, INVILGET",
        "2020-01, 2020-11, INVILGET",
        "2020-01, 2020-12, INVILGET",
    )
    fun `Given 7 months of omsorgsarbeid Then halvt ar med omsorg is INVILGET`(
        fom: YearMonth,
        tom: YearMonth,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom, tom, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-01, INVILGET",
        "2020-12, 2020-12, INVILGET",
        "2020-01, 2020-12, INVILGET",
        "2020-07, 2020-12, INVILGET",
    )
    fun `Given at least 1 months of omsorgsarbeid when child is 0 year old Then omsorg is INVILGET`(
        fom: YearMonth,
        tom: YearMonth,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom, tom, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2020),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "2021-01, 2021-01, INVILGET",
        "2021-12, 2021-12, INVILGET",
        "2021-01, 2021-12, INVILGET",
        "2021-07, 2021-12, INVILGET",
    )
    fun `Given months of omsorgsarbeid is 0 in omsorgsAr and 1 in omsorgsAr + 1 when omsorgsmottaker is born in omsorgsAr Then INVILGET`(
        fom: YearMonth,
        tom: YearMonth,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom, tom, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2020),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }


    @ParameterizedTest
    @CsvSource(
        "2019-12, 2020-01, AVSLAG",
        "2020-12, 2021-01, AVSLAG",
        "2019-11, 2020-06, AVSLAG",
        "2020-07, 2021-02, AVSLAG",
        "2019-02, 2021-02, INVILGET",
        "2019-12, 2021-01, INVILGET",
        "2019-02, 2020-07, INVILGET",
        "2019-12, 2020-07, INVILGET",
        "2020-06, 2021-07, INVILGET",
        "2020-06, 2021-01, INVILGET",
    )
    fun `Given 7 months of omsorgsarbeid When fom or tom overlap with omsorgsar Then halvt ar med omsorg is INVILGET`(
        fom: YearMonth,
        tom: YearMonth,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom, tom, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "2019-01, 2019-12",
        "2021-01, 2021-06",
    )
    fun `Given fom and tom dont overlap with omsorgsAr Then halvt ar med omsorg is AVSLAG`(
        fom: YearMonth,
        tom: YearMonth,
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom, tom, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(Avgjorelse.AVSLAG, vilkarsVurdering.avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, INVILGET",
        "2020-01, 2020-07, 2020-01, 2020-07, INVILGET",
        "2020-01, 2020-06, 2020-07, 2020-12, INVILGET",
        "2020-01, 2020-07, 2021-02, 2022-12, INVILGET",
        "2019-01, 2019-06, 2020-06, 2020-12, INVILGET",
        "2019-12, 2020-02, 2020-08, 2021-01, INVILGET",
        "2019-01, 2019-12, 2021-01, 2019-12, AVSLAG",
        "2019-01, 2019-06, 2021-06, 2019-12, AVSLAG",
        "2019-01, 2020-05, 2020-12, 2021-12, AVSLAG",
        "2019-01, 2020-04, 2020-11, 2021-12, AVSLAG",
        "2019-01, 2020-03, 2020-10, 2021-12, AVSLAG",
        "2019-01, 2020-02, 2020-09, 2021-12, AVSLAG",
        "2019-01, 2020-01, 2020-08, 2021-12, AVSLAG",
    )
    fun `Given 7 months of omsorgsarbeid When two utbetalings periodes Then halvt ar med omsorg is INVILGET`(
        fom1: YearMonth,
        tom1: YearMonth,
        fom2: YearMonth,
        tom2: YearMonth,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom1, tom1, Person(fnr = FNR_OMSORGSYTER), listOf()),
                    OmsorgsArbeid(fom2, tom2, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-07, 2020-01, 2020-07, 2020-01, 2020-06, INVILGET",
        "2020-06, 2020-12, 2020-06, 2020-12, 2020-07, 2020-12, INVILGET",
        "2019-01, 2020-01, 2020-03, 2020-05, 2020-10, 2020-12, INVILGET",
        "2020-01, 2020-04, 2020-06, 2020-06, 2020-11, 2021-12, INVILGET",
        "2019-01, 2020-02, 2020-04, 2020-04, 2020-10, 2021-12, AVSLAG",
        "2012-01, 2012-06, 2019-01, 2019-12, 2021-01, 2021-01, AVSLAG",
    )
    fun `Given 7 months of omsorgsarbeid When three utbetalings periodes Then halvt ar med omsorg is INVILGET`(
        fom1: YearMonth,
        tom1: YearMonth,
        fom2: YearMonth,
        tom2: YearMonth,
        fom3: YearMonth,
        tom3: YearMonth,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(fom1, tom1, Person(fnr = FNR_OMSORGSYTER), listOf()),
                    OmsorgsArbeid(fom2, tom2, Person(fnr = FNR_OMSORGSYTER), listOf()),
                    OmsorgsArbeid(fom3, tom3, Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER,2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }


    @Test
    fun `Given no utbetalingsperioder Then halvt ar med omsorg is AVSLAG`() {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        assertEquals(Avgjorelse.AVSLAG, vilkarsVurdering.avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "2000, 2000, INVILGET",
        "2005, 2000, INVILGET",
        "2006, 2000, AVSLAG",
    )
    fun `Given omsorgsmottaker younger than 6 years Then halvt ar med omsorg is INVILGET`(
        omsorgsAr: Int,
        fodselsArBarn: Int,
        expectedAvgjorelse: Avgjorelse
    ) {
        val vilkarsVurdering = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(
                    OmsorgsArbeid(YearMonth.of(omsorgsAr, Month.JANUARY), YearMonth.of(omsorgsAr, Month.DECEMBER), Person(fnr = FNR_OMSORGSYTER), listOf())
                ),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER, fodselsArBarn),
                omsorgsAr = omsorgsAr
            )
        )

        assertEquals(expectedAvgjorelse, vilkarsVurdering.avgjorelse)
    }

    private fun createPerson(fnr: String, fodselsAr: Int) = no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
        alleFnr = mutableSetOf(Fnr(fnr = fnr)),
        fodselsAr = fodselsAr
    )

    companion object {
        const val OMSORGS_AR_2020 = 2020
        const val FNR_OMSORGSYTER = "1234"
        const val FNR_OMSORGSMOTTAKER = "5678"
    }
}