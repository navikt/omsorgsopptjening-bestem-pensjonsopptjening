package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsArbeidsUtbetalingerOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidsUtbetalinger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.YearMonth

internal class HalvtArMedOmsorgTest {

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-01, false",
        "2020-01, 2020-02, false",
        "2020-01, 2020-03, false",
        "2020-01, 2020-04, false",
        "2020-01, 2020-05, false",
        "2020-01, 2020-06, true",
        "2020-01, 2020-07, true",
        "2020-01, 2020-08, true",
        "2020-01, 2020-09, true",
        "2020-01, 2020-10, true",
        "2020-01, 2020-11, true",
        "2020-01, 2020-12, true",
    )
    fun `Given 6 months of omsorgsarbeid When conducting vilkars vurdering halvt ar med omsorg Then true`(
        fom: YearMonth,
        tom: YearMonth,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(fom, tom)
                ),
                omsorgsAr = OMSORGS_AR_2020
            )
        ).utforVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }


    @ParameterizedTest
    @CsvSource(
        "2019-12, 2020-01, false",
        "2020-12, 2021-01, false",
        "2019-11, 2020-05, false",
        "2020-08, 2021-02, false",
        "2019-02, 2021-02, true",
        "2019-12, 2021-01, true",
        "2019-02, 2020-06, true",
        "2019-12, 2020-06, true",
        "2020-07, 2021-07, true",
        "2020-07, 2021-01, true",
    )
    fun `Given 6 months of omsorgsarbeid When fom or tom overlap with omsorgsar Then halvt ar med omsorg is true`(
        fom: YearMonth,
        tom: YearMonth,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(fom, tom)
                ),
                omsorgsAr = OMSORGS_AR_2020
            )
        ).utforVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }

    @ParameterizedTest
    @CsvSource(
        "2019-01, 2019-12",
        "2021-01, 2021-06",
    )
    fun `Given fom and tom dont overlap with omsorgsAr Then halvt ar med omsorg is false`(
        fom: YearMonth,
        tom: YearMonth,
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(fom, tom)
                ),
                omsorgsAr = OMSORGS_AR_2020
            )
        ).utforVilkarsVurdering()

        assertEquals(false, resultat.oppFyllerRegel)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, true",
        "2020-01, 2020-06, 2020-01, 2020-06, true",
        "2020-01, 2020-06, 2020-07, 2020-12, true",
        "2020-01, 2020-06, 2021-02, 2022-12, true",
        "2019-01, 2019-06, 2020-07, 2020-12, true",
        "2019-12, 2020-01, 2020-08, 2021-01, true",
        "2019-01, 2019-12, 2021-01, 2019-12, false",
        "2019-01, 2019-06, 2021-06, 2019-12, false",
        "2019-01, 2020-04, 2020-12, 2021-12, false",
        "2019-01, 2020-03, 2020-11, 2021-12, false",
        "2019-01, 2020-02, 2020-10, 2021-12, false",
    )
    fun `Given 6 months of omsorgsarbeid When two utbetalings periodes Then halvt ar med omsorg is true`(
        fom1: YearMonth,
        tom1: YearMonth,
        fom2: YearMonth,
        tom2: YearMonth,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(fom1, tom1),
                    OmsorgsArbeidsUtbetalinger(fom2, tom2)
                ),
                omsorgsAr = OMSORGS_AR_2020
            )
        ).utforVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-06, 2020-01, 2020-06, 2020-01, 2020-06, true",
        "2020-07, 2020-12, 2020-07, 2020-12, 2020-07, 2020-12, true",
        "2019-01, 2020-01, 2020-03, 2020-04, 2020-10, 2020-12, true",
        "2020-01, 2020-03, 2020-04, 2020-04, 2020-11, 2021-12, true",
        "2019-01, 2020-02, 2020-04, 2020-04, 2020-11, 2021-12, false",
        "2012-01, 2012-03, 2019-01, 2019-12, 2021-01, 2021-01, false",
    )
    fun `Given 6 months of omsorgsarbeid When three utbetalings periodes Then halvt ar med omsorg is true`(
        fom1: YearMonth,
        tom1: YearMonth,
        fom2: YearMonth,
        tom2: YearMonth,
        fom3: YearMonth,
        tom3: YearMonth,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(fom1, tom1),
                    OmsorgsArbeidsUtbetalinger(fom2, tom2),
                    OmsorgsArbeidsUtbetalinger(fom3, tom3)
                ),
                omsorgsAr = OMSORGS_AR_2020
            )
        ).utforVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }


    @Test
    fun `Given no utbetalingsperioder Then halvt ar med omsorg is false`() {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(),
                omsorgsAr = OMSORGS_AR_2020
            )
        ).utforVilkarsVurdering()

        assertEquals(false, resultat.oppFyllerRegel)
    }

    companion object {
        const val OMSORGS_AR_2020 = 2020
    }
}