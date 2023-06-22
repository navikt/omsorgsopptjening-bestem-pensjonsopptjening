package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FullOmsorgForBarnUnder6Test {

    @Test
    fun `Gitt en mottaker født utenfor omsorgsår når det er minst seks måneder full omsorg så invilget`() {
        FullOmsorgForBarnUnder6.vilkarsVurder(
            grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12125678910",
                    fodselsAr = 1999
                ),
                minstSeksMånederFullOmsorg = true
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)}
        }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det er minst en måned full omsorg så invilget`() {
        FullOmsorgForBarnUnder6.vilkarsVurder(
            grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = 2000
                ),
                minstEnMånedFullOmsorg = true
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)}
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det ikke er minst en måned full omsorg så avslag`() {
        FullOmsorgForBarnUnder6.vilkarsVurder(
            grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = 2000
                ),
                minstEnMånedFullOmsorg = false
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)}
    }

    @Test
    fun `Gitt en mottaker født I desember i omsorgsår når det er minst en måned full omsorg så invilget`() {
        FullOmsorgForBarnUnder6.vilkarsVurder(
            grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12125678910",
                    fodselsAr = 2000
                ),
                minstEnMånedOmsorgÅretEtterFødsel = true
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)}
    }

    @Test
    fun `Gitt en mottaker født I desember i omsorgsår når det ikke er minst en måned full omsorg så avslag`() {
        FullOmsorgForBarnUnder6.vilkarsVurder(
            grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12125678910",
                    fodselsAr = 2000
                ),
                minstEnMånedOmsorgÅretEtterFødsel = false
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)}
    }



    @Test
    fun `number of months with full omsorg`() {
        val omsorgsår = 2000
        listOf(0, 1, 2, 3, 4, 5, 6).forEach { monthsFullOmsorg ->
            FullOmsorgForBarnUnder6.vilkarsVurder(
                grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - 2
                    ),
                    minstSeksMånederFullOmsorg = monthsFullOmsorg > 6
                )
            ).also { vurdering ->
                assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                    assertEquals(
                        setOf(
                            Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1,
                            Lovparagraf.FTRL_K20_P8_L1_Ba_pkt3
                        ),
                        it.henvisninger
                    )
                }
            }

        }
        listOf(7, 8, 9, 10, 11, 12).forEach { monthsFullOmsorg ->
            FullOmsorgForBarnUnder6.vilkarsVurder(
                grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - 2
                    ),
                    minstSeksMånederFullOmsorg = monthsFullOmsorg > 6
                )
            ).also {
                assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
            }
        }
    }

    @Test
    fun `no requirements met`() {
        val omsorgsår = 2000
        FullOmsorgForBarnUnder6.vilkarsVurder(
            grunnlag = FullOmsorgForBarnUnder6.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = omsorgsår - 6
                ),
                minstSeksMånederFullOmsorg = false
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                assertEquals(
                    setOf(
                        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt1,
                        Lovparagraf.FTRL_K20_P8_L1_Ba_pkt3
                    ),
                    it.henvisninger
                )
            }
        }
    }
}
