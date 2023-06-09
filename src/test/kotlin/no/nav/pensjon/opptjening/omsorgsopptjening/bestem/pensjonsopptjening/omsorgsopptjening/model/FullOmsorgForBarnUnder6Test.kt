package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FullOmsorgForBarnUnder6Test {

    @Test
    fun `required number of months for child between 0 and 6`() {
        val omsorgsår = 2000
        listOf(6).forEach { childAge ->
            FullOmsorgForBarnUnder6().vilkarsVurder(
                grunnlag = OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - childAge
                    ),
                    minstSeksMånederFullOmsorg = true
                )
            ).also { vurdering ->
                assertInstanceOf(FullOmsorgForBarnUnder6Avslag::class.java, vurdering.utfall).also {
                    assertEquals(listOf(AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5), it.årsaker)
                }
            }

        }
        listOf(1, 2, 3, 4, 5).forEach { childAge ->
            FullOmsorgForBarnUnder6().vilkarsVurder(
                grunnlag = OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - childAge
                    ),
                    minstSeksMånederFullOmsorg = true
                )
            ).also {
                assertInstanceOf(FullOmsorgForBarnUnder6Innvilget::class.java, it.utfall)
            }
        }
    }

    @Test
    fun `number of months with full omsorg`() {
        val omsorgsår = 2000
        listOf(0, 1, 2, 3, 4, 5, 6).forEach { monthsFullOmsorg ->
            FullOmsorgForBarnUnder6().vilkarsVurder(
                grunnlag = OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - 2
                    ),
                    minstSeksMånederFullOmsorg = monthsFullOmsorg > 6
                )
            ).also { vurdering ->
                assertInstanceOf(FullOmsorgForBarnUnder6Avslag::class.java, vurdering.utfall).also {
                    assertEquals(listOf(AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG), it.årsaker)
                }
            }

        }
        listOf(7, 8, 9, 10, 11, 12).forEach { monthsFullOmsorg ->
            FullOmsorgForBarnUnder6().vilkarsVurder(
                grunnlag = OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - 2
                    ),
                    minstSeksMånederFullOmsorg = monthsFullOmsorg > 6
                )
            ).also {
                assertInstanceOf(FullOmsorgForBarnUnder6Innvilget::class.java, it.utfall)
            }
        }
    }

    @Test
    fun `no requirements met`() {
        val omsorgsår = 2000
        FullOmsorgForBarnUnder6().vilkarsVurder(
            grunnlag = OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = omsorgsår - 6
                ),
                minstSeksMånederFullOmsorg = false
            )
        ).also { vurdering ->
            assertInstanceOf(FullOmsorgForBarnUnder6Avslag::class.java, vurdering.utfall).also {
                assertEquals(
                    listOf(AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG, AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5),
                    it.årsaker
                )
            }
        }
    }
}