package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsArbeidsUtbetalingerOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
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
    fun `Given fom first day of month When calling vilkarsVurder halvt ar med omsorg Then return months of omsorgsarbeid omsorgsAr`(
        fom: String,
        tom: String,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(
                        YearMonth.parse(fom),
                        YearMonth.parse(tom)
                    )
                ),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }

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
    fun `Given fom last day og month When calling monthsOfOmsorg Then return months of omsorgsarbeid omsorgsAr`(
        fom: String,
        tom: String,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(
                        YearMonth.parse(fom),
                        YearMonth.parse(tom)
                    )
                ),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }


    @ParameterizedTest
    @CsvSource(
        "2019-02, 2021-02, true",
        "2019-12, 2021-01, true",
        "2019-12, 2020-01, false",
        "2020-12, 2021-01, false",
        "2019-02, 2020-06, true",
        "2019-12, 2020-06, true",
        "2020-07, 2021-07, true",
        "2020-07, 2021-01, true",
    )
    fun `Given fom or tom overlap with omsorgsAr Then return months in omsorgsAr`(
        fom: String,
        tom: String,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(
                        YearMonth.parse(fom),
                        YearMonth.parse(tom)
                    )
                ),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
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
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(
                        YearMonth.parse(fom),
                        YearMonth.parse(tom)
                    )
                ),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(false, resultat.oppFyllerRegel)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, true",
        "2020-01, 2020-06, 2020-07, 2020-12, true",
        "2019-01, 2020-06, 2020-07, 2022-12, true",
        "2020-01, 2020-12, 2021-01, 2019-12, true",
        "2020-01, 2020-06, 2020-08, 2020-12, true",
        "2019-01, 2020-06, 2020-08, 2021-01, true",
        "2019-01, 2019-12, 2021-01, 2019-12, false",
        "2019-01, 2019-06, 2021-06, 2019-12, false",

        )
    fun `Given given two utbetalings periodes Then count months overlaping with omsorgsAr`(
        fom1: String,
        tom1: String,
        fom2: String,
        tom2: String,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(YearMonth.parse(fom1), YearMonth.parse(tom1)),
                    OmsorgsArbeidsUtbetalinger(YearMonth.parse(fom2), YearMonth.parse(tom2))
                ),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }

    @ParameterizedTest
    @CsvSource(
        "2020-01, 2020-12, 2020-01, 2020-12, 2020-01, 2020-12, true",
        "2019-01, 2021-01, 2019-01, 2021-01, 2019-12, 2020-12, true",
        "2020-01, 2020-03, 2020-04, 2020-06, 2020-10, 2021-12, true",
        "2012-01, 2012-03, 2019-01, 2019-12, 2021-01, 2021-01, false",
    )
    fun `Given given thre utbetalings periodes Then count months overlaping with omsorgsAr`(
        fom1: String,
        tom1: String,
        fom2: String,
        tom2: String,
        fom3: String,
        tom3: String,
        expectedInvilget: Boolean
    ) {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(
                    OmsorgsArbeidsUtbetalinger(YearMonth.parse(fom1), YearMonth.parse(tom1)),
                    OmsorgsArbeidsUtbetalinger(YearMonth.parse(fom2), YearMonth.parse(tom2)),
                    OmsorgsArbeidsUtbetalinger(YearMonth.parse(fom3), YearMonth.parse(tom3))
                ),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }


    @Test
    fun `Given omsorgsarbeid without utbetalingsperioder When calling monthsOfOmsorg Then 0`() {
        val resultat = HalvtArMedOmsorg().vilkarsVurder(
            grunnlag = OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                omsorgsArbeidsUtbetalinger = listOf(),
                omsorgsAr = 2020
            )
        ).utførVilkarsVurdering()

        assertEquals(false, resultat.oppFyllerRegel)
    }
}