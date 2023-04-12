package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.hentOmsorgForBarnUnder6VilkarsVurderinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.IndividuellVilkarsvurdering
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Month
import java.time.YearMonth

internal class IndividuellVilkarsvurderingTest {

    private val individuellVilkarsvurdering: IndividuellVilkarsvurdering = IndividuellVilkarsvurdering()

    @Test
    fun `Given omsorgs arbeid for seven months When calling fastsettOmsorgsOpptjening Then INVILGET`() {
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 2005)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JULY),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf(omsorgsmottaker)
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)

        assertEquals(Utfall.INVILGET, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
    }

    @Test
    fun `Given omsorgs arbeid for less than seven months When calling fastsettOmsorgsOpptjening Then AVSLAG`() {
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 2015)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JUNE),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf(omsorgsmottaker)
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)
        assertEquals(Utfall.AVSLAG, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
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
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, fodselsAr)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, 1995)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf(omsorgsmottaker)
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)
        assertEquals(expectedUtfall, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
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
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, 1960)
        val omsorgsmottaker = createPerson(FNR_OMSORGSMOTTAKER, fodselsAr)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf(omsorgsmottaker)
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)
        assertEquals(expectedUtfall, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
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
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, 1960)
        val omsorgsmottaker1 = createPerson(FNR_OMSORGSMOTTAKER, fodselsArOmsorgsMottaker1)
        val omsorgsmottaker2 = createPerson(FNR_OMSORGSMOTTAKER_2, fodselsArOmsorgsMottaker2)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = omsorgsAr,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                    tom = YearMonth.of(omsorgsAr, Month.JULY),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf(omsorgsmottaker1, omsorgsmottaker2)
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)
        assertEquals(expectedUtfall, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
    }

    @Test
    fun `Given no omsorgsmottaker When calling fastsettOmsorgsOpptjening Then AVSLAG`() {
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, 1960)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2006,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2006, Month.JANUARY),
                    tom = YearMonth.of(2006, Month.JULY),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf()
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)
        assertEquals(Utfall.AVSLAG, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
    }

    @Test
    fun `Given omsorgs arbeid for seven months When having omsorgs for more than one person Then INVILGET`() {
        val omsorgsyter = createPerson(FNR_OMSORGSGIVER, 1990)
        val omsorgsmottaker1 = createPerson(FNR_OMSORGSMOTTAKER, 2005)
        val omsorgsmottaker2 = createPerson(FNR_OMSORGSMOTTAKER_2, 2007)
        val omsorgsmottaker3 = createPerson(FNR_OMSORGSMOTTAKER_3, 1995)

        val omsorgsArbeidSnapshot = creatOmsorgsArbeidSnapshot(
            omsorgsAr = 2010,
            omsorgsyter = omsorgsyter,
            omsorgsarbeidPerioder = listOf(
                createOmsorgsArbeid(
                    fom = YearMonth.of(2010, Month.JANUARY),
                    tom = YearMonth.of(2010, Month.JULY),
                    omsorgsyter = omsorgsyter,
                    omsorgsmottakere = listOf(omsorgsmottaker1, omsorgsmottaker2, omsorgsmottaker3)
                )
            )
        )

        val individueltVilkarsResultat = individuellVilkarsvurdering.vilkarsvurder(omsorgsArbeidSnapshot)

        val barnUnder6Invilget = hentOmsorgForBarnUnder6VilkarsVurderinger(individueltVilkarsResultat.individueltVilkarsresultat!!).filter { it.utfall == Utfall.INVILGET }
        val barnUnder6IkkeInvilget = hentOmsorgForBarnUnder6VilkarsVurderinger(individueltVilkarsResultat.individueltVilkarsresultat!!).filter { it.utfall != Utfall.INVILGET }

        assertEquals(barnUnder6Invilget.size, 2)
        assertEquals(1, barnUnder6Invilget.filter { it.grunnlag.omsorgsmottaker == omsorgsmottaker1}.size)
        assertEquals(1, barnUnder6Invilget.filter { it.grunnlag.omsorgsmottaker == omsorgsmottaker2}.size)

        assertEquals(barnUnder6IkkeInvilget.size, 1)
        assertEquals(1, barnUnder6IkkeInvilget.filter { it.grunnlag.omsorgsmottaker == omsorgsmottaker3}.size)

        assertEquals(Utfall.INVILGET, individueltVilkarsResultat.individueltVilkarsresultat!!.utfall)
    }

    private fun creatOmsorgsArbeidSnapshot(
        omsorgsAr: Int,
        omsorgsyter: Person,
        omsorgsarbeidPerioder: List<OmsorgsarbeidPeriode>
    ) =

        OmsorgsarbeidSnapshot(
            omsorgsAr = omsorgsAr,
            kjoreHashe = "xxx",
            omsorgsyter = omsorgsyter,
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
        omsorgsyter: Person,
        omsorgsmottakere: List<Person>
    ) = OmsorgsarbeidPeriode(
        fom = fom,
        tom = tom,
        prosent = 100,
        omsorgsytere =  listOf(omsorgsyter),
        omsorgsmottakere = omsorgsmottakere
    )


    private fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
        Person(
            alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toMutableSet().apply { add(Fnr(fnr = gjeldendeFnr, gjeldende = true)) },
            fodselsAr = fodselsAr
        )

    companion object {
        const val FNR_OMSORGSGIVER: String = "12345678902"
        const val FNR_OMSORGSMOTTAKER: String = "55555555555"
        const val FNR_OMSORGSMOTTAKER_2: String = "6666666666"
        const val FNR_OMSORGSMOTTAKER_3: String = "4444444444"
    }
}