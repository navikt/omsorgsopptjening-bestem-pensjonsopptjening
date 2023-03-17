package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HalvtArMedOmsorgVisitorTest {

    @Test
    fun ape() {
        val halvtAr1 = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER_1, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        val halvtAr2 = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER_2, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        val halvtAr3 = HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = HalvtArMedOmsorgGrunnlag(
                omsorgsArbeid = listOf(),
                omsorgsMottaker = createPerson(FNR_OMSORGSMOTTAKER_3, 2015),
                omsorgsAr = OMSORGS_AR_2020
            )
        )

        val vilkarsvurdering = og(
            eller(
                halvtAr2,
                dummyVilkar.vilkarsVurder(Avgjorelse.INVILGET),
                dummyVilkar.vilkarsVurder(Avgjorelse.INVILGET),
            ),
            eller(
                dummyVilkar.vilkarsVurder(Avgjorelse.INVILGET),
                halvtAr1,
                dummyVilkar.vilkarsVurder(Avgjorelse.INVILGET),
            ),
            eller(
                dummyVilkar.vilkarsVurder(Avgjorelse.INVILGET),
                dummyVilkar.vilkarsVurder(Avgjorelse.INVILGET),
                halvtAr3
            ),
        )

        val halvtArMedOmsorgResultat = hentHalvtArMedOmsorgVilkarsVurderinger(vilkarsvurdering)

        assertEquals(3, halvtArMedOmsorgResultat.size)
        assertTrue(halvtArMedOmsorgResultat.all { it.vilkar is HalvtArMedOmsorgForBarnUnder6 })
        assertTrue(halvtArMedOmsorgResultat.map { it.grunnlag }.contains(halvtAr1.grunnlag))
        assertTrue(halvtArMedOmsorgResultat.map { it.grunnlag }.contains(halvtAr2.grunnlag))
        assertTrue(halvtArMedOmsorgResultat.map { it.grunnlag }.contains(halvtAr3.grunnlag))
    }


    private fun createPerson(fnr: String, fodselsAr: Int) =
        no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person(
            alleFnr = mutableSetOf(Fnr(fnr = fnr)),
            fodselsAr = fodselsAr
        )

    companion object {
        const val FNR_OMSORGSMOTTAKER_1 = "1111"
        const val FNR_OMSORGSMOTTAKER_2 = "2222"
        const val FNR_OMSORGSMOTTAKER_3 = "3333"
        const val OMSORGS_AR_2020 = 2020

        private val dummyVilkar = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            avgjorelsesFunksjon = fun(avgjorelse: Avgjorelse) = avgjorelse
        )
    }
}