package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.AnnenPart
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagDeltOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Month
import java.time.YearMonth

class DeltOmsorgForBarnUnder6Test {

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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2015,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom,
                        tom = tom,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf()
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2020,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom,
                        tom = tom,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf()
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2020,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom,
                        tom = tom,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf()
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2015,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom,
                        tom = tom,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf()
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
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
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2015,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom,
                        tom = tom,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf()
            )
        )

        assertEquals(Utfall.AVSLAG, vilkarsVurdering.utfall)
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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2015,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom1,
                        tom = tom1,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    ),
                    OmsorgsarbeidPeriode(
                        fom = fom2,
                        tom = tom2,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf()
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2015,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = fom1,
                        tom = tom1,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    ),
                    OmsorgsarbeidPeriode(
                        fom = fom2,
                        tom = tom2,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    ),
                    OmsorgsarbeidPeriode(
                        fom = fom3,
                        tom = tom3,
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988),
                        omsorgsmottakere = listOf()
                    ),
                ),
                andreParter = listOf()
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
    }


    @Test
    fun `Given no utbetalingsperioder Then halvt ar med omsorg is AVSLAG`() {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = AR_2020,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2015,
                omsorgsArbeid50Prosent = listOf(),
                andreParter = listOf()
            )
        )

        assertEquals(Utfall.AVSLAG, vilkarsVurdering.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "2005, INVILGET",
        "2006, AVSLAG",
    )
    fun `Given omsorgsmottaker older than 5 years Then AVSLAG`(
        omsorgsAr: Int,
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = DeltOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = GrunnlagDeltOmsorgForBarnUnder6(
                omsorgsAr = omsorgsAr,
                omsorgsyter = omsorgsyter_1988,
                omsorgsmottaker = omsorgsmottaker_2000,
                omsorgsArbeid50Prosent = listOf(
                    OmsorgsarbeidPeriode(
                        fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                        tom = YearMonth.of(omsorgsAr, Month.DECEMBER),
                        prosent = 50,
                        omsorgsytere = listOf(omsorgsyter_1988, omsorgsyter_1977),
                        omsorgsmottakere = listOf()
                    )
                ),
                andreParter = listOf(
                    AnnenPart(
                        omsorgsyter = omsorgsyter_1977,
                        omsorgsArbeid50Prosent = listOf(),
                        harInvilgetOmsorgForUrelaterBarn = true
                    )
                )
            )
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
    }

    companion object {
        private const val AR_2020 = 2020

        private val omsorgsyter_1988 = Person(alleFnr = mutableSetOf(Fnr(fnr = "11111988", gjeldende =  true)), fodselsAr = 1988)
        private val omsorgsyter_1977 = Person(alleFnr = mutableSetOf(Fnr(fnr = "11111977", gjeldende =  true)), fodselsAr = 1977)

        private val omsorgsmottaker_2015 = Person(alleFnr = mutableSetOf(Fnr(fnr = "22222015", gjeldende =  true)), fodselsAr = 2015)
        private val omsorgsmottaker_2020 = Person(alleFnr = mutableSetOf(Fnr(fnr = "33332020", gjeldende =  true)), fodselsAr = 2020)
        private val omsorgsmottaker_2000 = Person(alleFnr = mutableSetOf(Fnr(fnr = "44442000", gjeldende =  true)), fodselsAr = 2000)
    }
}