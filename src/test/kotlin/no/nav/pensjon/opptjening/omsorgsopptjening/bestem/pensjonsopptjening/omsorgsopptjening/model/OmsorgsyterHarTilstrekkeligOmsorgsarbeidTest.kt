package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OmsorgsyterHarTilstrekkeligOmsorgsarbeidTest {

    @Test
    fun `Gitt en mottaker født utenfor omsorgsår når det er minst seks måneder full omsorg så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12125678910",
                    fodselsAr = 1999
                ),
                antallMåneder = 6
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det er minst en måned full omsorg så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = 2000
                ),
                antallMåneder = 1
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det ikke er minst en måned full omsorg så avslag`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = 2000
                ),
                antallMåneder = 0
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I desember i omsorgsår når det er minst en måned full omsorg så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12125678910",
                    fodselsAr = 2000
                ),
                antallMåneder = 1
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I desember i omsorgsår når det ikke er minst en måned full omsorg så avslag`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår(
                omsorgsAr = 2000,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12125678910",
                    fodselsAr = 2000
                ),
                antallMåneder = 0
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)
        }
    }


    @Test
    fun `number of months with full omsorg`() {
        val omsorgsår = 2000
        listOf(0, 1, 2, 3, 4, 5).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - 2
                    ),
                    antallMåneder = monthsFullOmsorg
                )
            ).also { vurdering ->
                assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                    assertEquals(
                        setOf(
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum,
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum
                        ),
                        it.henvisninger
                    )
                }
            }

        }
        listOf(6, 7, 8, 9, 10, 11, 12).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                    omsorgsAr = omsorgsår,
                    omsorgsmottaker = PersonMedFødselsår(
                        fnr = "12345678910",
                        fodselsAr = omsorgsår - 2
                    ),
                    antallMåneder = monthsFullOmsorg
                )
            ).also {
                assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
            }
        }
    }

    @Test
    fun `no requirements met`() {
        val omsorgsår = 2000
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår(
                omsorgsAr = omsorgsår,
                omsorgsmottaker = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = omsorgsår - 6
                ),
                antallMåneder = 3
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                assertEquals(
                    setOf(
                        JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum,
                        JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum
                    ),
                    it.henvisninger
                )
            }
        }
    }
}
