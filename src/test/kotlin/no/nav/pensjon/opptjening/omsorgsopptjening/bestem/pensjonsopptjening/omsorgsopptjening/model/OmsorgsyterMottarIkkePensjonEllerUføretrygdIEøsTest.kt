package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsTest {
    @Test
    fun `innvilget dersom bruker ikke har noen ytelsesmåneder`() {
        OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.vilkarsVurder(
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(setOf(januar(2024).omsorgsmåned(DomainOmsorgstype.Barnetrygd.Full))),
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
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(Periode(januar(2024), mars(2024)).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag(
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                ytelsemåneder = år(2024).ytelseMåneder(),
                landstilknytningmåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java, it.utfall)
        }
    }
}

fun Periode.ytelseMåneder(): Ytelsemåneder {
    return Ytelsemåneder(alleMåneder())
}