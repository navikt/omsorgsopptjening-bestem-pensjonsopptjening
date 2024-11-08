package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.omsorgsmånederHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.august
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.assertj.core.api.Assertions.assertThat
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
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JUNE)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født utenfor omsorgsår når det er seks måneder delt omsorg så ubestemt`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JUNE)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker med hjelpestønad seks måneder delt omsorg så ubestemt`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder =
                Periode(
                    YearMonth.of(2000, Month.JANUARY),
                    YearMonth.of(2000, Month.JUNE)
                ).omsorgsmånederHjelpestønad(DomainOmsorgstype.Barnetrygd.Delt),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det er minst en måned full omsorg så invilget`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JANUARY)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det er en måned delt omsorg så ubestemt`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JANUARY)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `Gitt en mottaker født I omsorgsår når det ikke er minst en måned full omsorg så avslag`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
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
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2001, Month.JANUARY),
                        YearMonth.of(2001, Month.JANUARY)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
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
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår

            ),
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `innvilget hvis kombinasjon av delt og full men tilstrekkelig antall full`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2020), juni(2020)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full) +
                            Periode(juli(2020), desember(2020)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår

            ),
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `ubestemt hvis kombinajon av delt og full, men ikke tilstrekkelig full`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2020), mai(2020)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full) +
                            Periode(juni(2020), desember(2020)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår

            ),
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `ubestemt hvis delt og tilstrekkelig antall måneder`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(januar(2020), desember(2020)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår

            ),
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `avslag hvis bare delt omsorg og ikke tilstrekkelig antall`() {
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(juli(2020), august(2020)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår

            ),
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, vurdering.utfall)
        }
    }


    @Test
    fun `number of months with full omsorg`() {
        val omsorgsår = 2000
        listOf(0, 1, 2, 3, 4, 5).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                    omsorgsmåneder = if (monthsFullOmsorg == 0) Omsorgsmåneder.Barnetrygd(
                        emptySet()
                    ) else Omsorgsmåneder.Barnetrygd(
                        Periode(
                            YearMonth.of(omsorgsår, Month.JANUARY),
                            YearMonth.of(
                                omsorgsår,
                                monthsFullOmsorg
                            )
                        ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                    ),
                    antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
                )
            ).also { vurdering ->
                assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                    assertEquals(
                        setOf(
                            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
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
                    omsorgsmåneder = if (monthsFullOmsorg == 0) Omsorgsmåneder.Barnetrygd(
                        emptySet()
                    ) else Omsorgsmåneder.Barnetrygd(
                        Periode(
                            YearMonth.of(omsorgsår, Month.JANUARY),
                            YearMonth.of(
                                omsorgsår,
                                monthsFullOmsorg
                            )
                        ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                    ),
                    antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
                )
            ).also {
                assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
            }
        }
    }

    @Test
    fun `number of months with full omsorg hjelpestønad`() {
        val omsorgsår = 2000
        listOf(0, 1, 2, 3, 4, 5).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                    omsorgsmåneder = if (monthsFullOmsorg == 0) Omsorgsmåneder.BarnetrygdOgHjelpestønad(
                        emptySet()
                    ) else
                        Periode(
                            YearMonth.of(omsorgsår, Month.JANUARY),
                            YearMonth.of(
                                omsorgsår,
                                monthsFullOmsorg
                            )
                        ).omsorgsmånederHjelpestønad(DomainOmsorgstype.Barnetrygd.Full),
                    antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
                )
            ).also { vurdering ->
                assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                    assertEquals(
                        setOf(
                            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum,
                        ),
                        it.henvisninger
                    )
                }
            }

        }
        listOf(6, 7, 8, 9, 10, 11, 12).forEach { monthsFullOmsorg ->
            OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
                grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                    omsorgsmåneder = if (monthsFullOmsorg == 0) Omsorgsmåneder.BarnetrygdOgHjelpestønad(
                        emptySet()
                    ) else Periode(
                        YearMonth.of(omsorgsår, Month.JANUARY), YearMonth.of(omsorgsår, monthsFullOmsorg)
                    ).omsorgsmånederHjelpestønad(DomainOmsorgstype.Barnetrygd.Full),
                    antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
                )
            ).also {
                assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall).also {
                    assertEquals(
                        setOf(
                            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum,
                        ),
                        it.henvisninger
                    )
                }
            }
        }
    }

    @Test
    fun `no requirements met`() {
        val omsorgsår = 2000
        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(omsorgsår, Month.JANUARY),
                        YearMonth.of(omsorgsår, Month.MARCH)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vurdering.utfall).also {
                assertEquals(
                    setOf(
                        JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
                        JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum,
                    ),
                    it.henvisninger
                )
            }
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full) +
                    Periode(oktober(2000), desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
        )

        val relevant = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = alle,
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertThat(vurdering.grunnlag.omsorgsmåneder()).isEqualTo(relevant)
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan ikke godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                    Periode(oktober(2000), desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(
            grunnlag = OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag(
                omsorgsmåneder = alle,
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertThat(vurdering.grunnlag.omsorgsmåneder()).isEqualTo(alle)
        }
    }
}
