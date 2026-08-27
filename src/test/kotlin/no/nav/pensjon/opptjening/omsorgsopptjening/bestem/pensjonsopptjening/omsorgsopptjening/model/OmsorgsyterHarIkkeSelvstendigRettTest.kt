package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilSelvstendigRettMåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth

class OmsorgsyterHarIkkeSelvstendigRettTest {

    @Test
    fun `innvilget dersom ingen måneder med selvstendig rett`() {
        OmsorgsyterHarIkkeSelvstendigRett.vilkarsVurder(
            grunnlag = OmsorgsyterHarIkkeSelvstendigRett.Grunnlag.new(
                selvstendigRettMåneder = SelvstendigRettMåneder.none(),
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
    fun `avslag dersom alle måneder med selvstendig rett`() {
        OmsorgsyterHarIkkeSelvstendigRett.vilkarsVurder(
            grunnlag = OmsorgsyterHarIkkeSelvstendigRett.Grunnlag.new(
                selvstendigRettMåneder = Periode(
                    YearMonth.of(2000, Month.JANUARY),
                    YearMonth.of(2000, Month.JUNE)
                ).tilSelvstendigRettMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JUNE)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `avslag dersom antall måneder med selvstendig rett er større enn 6`() {
        OmsorgsyterHarIkkeSelvstendigRett.vilkarsVurder(
            grunnlag = OmsorgsyterHarIkkeSelvstendigRett.Grunnlag.new(
                selvstendigRettMåneder = Periode(
                    YearMonth.of(2000, Month.JANUARY),
                    YearMonth.of(2000, Month.JULY)
                ).tilSelvstendigRettMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JUNE)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, vurdering.utfall)
        }
    }

    @Test
    fun `innvilget dersom blanding av selvstendig rett og ikke`() {
        OmsorgsyterHarIkkeSelvstendigRett.vilkarsVurder(
            grunnlag = OmsorgsyterHarIkkeSelvstendigRett.Grunnlag.new(
                selvstendigRettMåneder = Periode(
                    juli(2000),
                    desember(2000)
                ).tilSelvstendigRettMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        YearMonth.of(2000, Month.JANUARY),
                        YearMonth.of(2000, Month.JUNE)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also { vurdering ->
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java, vurdering.utfall)
        }
    }
}