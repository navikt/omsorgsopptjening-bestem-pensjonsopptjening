package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ytelseMåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsTest {
    @Test
    fun `innvilget dersom bruker ikke har noen ytelsesmåneder`() {
        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(setOf(januar(2024).tilOmsorgsmåned(DomainOmsorgstype.Barnetrygd.Full))),
                ytelsemåneder = Ytelsemåneder(emptySet()),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom bruker har noen ytelsesmåneder i eøs, men tilstrekkelig antall omsorgsmåneder utover disse`() {
        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                ytelsemåneder = Periode(januar(2024), februar(2024)).ytelseMåneder(),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Eøs.NorgeSekundærland),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `ubestemt dersom alle omsorgsmåneder er ytelesmåneder i eøs`() {
        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                ytelsemåneder = år(2024).ytelseMåneder(),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom alle omsorgsmåneder er ytelesmåneder i eøs og antall ikke er tilstrekkelig`() {
        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(Periode(januar(2024), mars(2024)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                ytelsemåneder = Periode(januar(2024), mars(2024)).ytelseMåneder(),
                landstilknytningmåneder = Periode(
                    januar(2024),
                    mars(2024)
                ).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom ingen ytelsesmåneder i eøs`() {
        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                ytelsemåneder = år(2024).ytelseMåneder(),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full) +
                    Periode(oktober(2000), Periode.desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
        )

        val relevant = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = alle,
                ytelsemåneder = år(2024).ytelseMåneder(),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.grunnlag.omsorgsmåneder()).isEqualTo(relevant)
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan ikke godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                    Periode(oktober(2000), Periode.desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.new(
                omsorgsmåneder = alle,
                ytelsemåneder = år(2024).ytelseMåneder(),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertThat(it.grunnlag.omsorgsmåneder()).isEqualTo(alle)
        }
    }
}

