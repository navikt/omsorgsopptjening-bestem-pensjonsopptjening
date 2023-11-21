package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals

class OmsorgsyterHarTilstrekkeligOmsorgsarbeidTest {

    @Test
    fun `Gitt en mottaker født utenfor omsorgsår når det er minst seks måneder full omsorg så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JUNE)
                    ).alleMåneder()
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det er minst en måned full omsorg så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JANUARY)
                    ).alleMåneder()
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det ikke er minst en måned full omsorg så avslag`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = Omsorgsmåneder.Barnetrygd(
                    (emptySet())
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I desember i omsorgsår når det er minst en måned full omsorg i påfølgende år så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2001, Month.JANUARY),
                        YearMonth.of(2001, Month.JANUARY)
                    ).alleMåneder()
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I desember i omsorgsår når det ikke er minst en måned full omsorg i påfølgende år så avslag`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = Omsorgsmåneder.Barnetrygd(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår

            ),
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)
        }
    }


    @Test
    fun `number of months with full omsorg`() {
        val omsorgsår = 2000
        listOf(0, 1, 2, 3, 4, 5).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                    omsorgsytersOmsorgsmånederForOmsorgsmottaker = if (monthsFullOmsorg == 0) Omsorgsmåneder.Barnetrygd(
                        emptySet()
                    ) else Omsorgsmåneder.Barnetrygd(
                        Periode(
                            YearMonth.of(omsorgsår, Month.JANUARY),
                            YearMonth.of(
                                omsorgsår,
                                monthsFullOmsorg
                            )
                        ).alleMåneder()
                    ),
                    antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
                )
            ).also { vurdering ->
                assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                    assertEquals(
                        setOf(
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum,
                        ),
                        it.henvisninger
                    )
                }
            }

        }
        listOf(6, 7, 8, 9, 10, 11, 12).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                    omsorgsytersOmsorgsmånederForOmsorgsmottaker = if (monthsFullOmsorg == 0) Omsorgsmåneder.Barnetrygd(
                        emptySet()
                    ) else Omsorgsmåneder.Barnetrygd(
                        Periode(
                            YearMonth.of(omsorgsår, Month.JANUARY),
                            YearMonth.of(
                                omsorgsår,
                                monthsFullOmsorg
                            )
                        ).alleMåneder()
                    ),
                    antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
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
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsytersOmsorgsmånederForOmsorgsmottaker = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(omsorgsår, Month.JANUARY),
                        YearMonth.of(omsorgsår, Month.MARCH)
                    ).alleMåneder()
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                assertEquals(
                    setOf(
                        JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum,
                    ),
                    it.henvisninger
                )
            }
        }
    }
}
