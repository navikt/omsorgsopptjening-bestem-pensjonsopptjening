package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HalvtArMedOmsorgVisitorTest {

    @Test
    fun ape() {
        val halvtAr1 = vilkarsvurderingOmsorgForBarnUnder6(FNR_OMSORGSMOTTAKER_1)
        val halvtAr2 = vilkarsvurderingOmsorgForBarnUnder6(FNR_OMSORGSMOTTAKER_2)
        val halvtAr3 = vilkarsvurderingOmsorgForBarnUnder6(FNR_OMSORGSMOTTAKER_3)

        val vilkarsvurdering = og(
            eller(
                halvtAr2,
                dummyVilkar.vilkarsVurder(Utfall.INVILGET),
                dummyVilkar.vilkarsVurder(Utfall.INVILGET),
            ),
            eller(
                dummyVilkar.vilkarsVurder(Utfall.INVILGET),
                halvtAr1,
                dummyVilkar.vilkarsVurder(Utfall.INVILGET),
            ),
            eller(
                dummyVilkar.vilkarsVurder(Utfall.INVILGET),
                dummyVilkar.vilkarsVurder(Utfall.INVILGET),
                halvtAr3
            ),
        )

        val halvtArMedOmsorgResultat = vilkarsvurdering.hentVilkarsVurderingerFullOmsorgForBarnUnder6()

        assertEquals(3, halvtArMedOmsorgResultat.size)
        assertTrue(halvtArMedOmsorgResultat.all { it.vilkar is FullOmsorgForBarnUnder6 })
        assertTrue(halvtArMedOmsorgResultat.map { it.grunnlag }.contains(halvtAr1.grunnlag))
        assertTrue(halvtArMedOmsorgResultat.map { it.grunnlag }.contains(halvtAr2.grunnlag))
        assertTrue(halvtArMedOmsorgResultat.map { it.grunnlag }.contains(halvtAr3.grunnlag))
    }


    private fun vilkarsvurderingOmsorgForBarnUnder6(fnrOmsorgsmottaker: String) =
        FullOmsorgForBarnUnder6().vilkarsVurder(
            GrunnlagOmsorgForBarnUnder6(
                omsorgsAr = 2020,
                omsorgsmottaker = createPerson(fnrOmsorgsmottaker),
                utfallPersonVilkarsvurdering = Utfall.INVILGET,
                omsorgsArbeid100Prosent = listOf(),
            )
        )

    private fun createPerson(fnr: String) = Person(alleFnr = mutableSetOf(Fnr(fnr = fnr)), fodselsAr = 1988)

    companion object {
        const val FNR_OMSORGSMOTTAKER_1 = "1111"
        const val FNR_OMSORGSMOTTAKER_2 = "2222"
        const val FNR_OMSORGSMOTTAKER_3 = "3333"

        private val dummyVilkar = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            utfallsFunksjon = fun(utfall: Utfall) = utfall
        )
    }
}