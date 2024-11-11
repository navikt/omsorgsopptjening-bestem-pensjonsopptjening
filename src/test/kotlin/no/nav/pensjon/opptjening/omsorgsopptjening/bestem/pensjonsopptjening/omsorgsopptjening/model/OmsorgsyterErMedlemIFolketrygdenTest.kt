package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.august
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OmsorgsyterErMedlemIFolketrygdenTest {

    @Test
    fun `innvilget dersom ingen omsorgsmåneder uten medlemskap`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = emptySet(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    setOf(
                        januar(2024).tilOmsorgsmåned(
                            DomainOmsorgstype.Barnetrygd.Full
                        )
                    )
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom man har tilstrekkelig antall omsorgsmåneder utover de månedene man ikke er medlem`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), april(2024)).alleMåneder(),
                pliktigEllerFrivillig = emptySet(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom man har tilstrekkelig antall omsorgsmåneder utover de månedene man ikke er medlem - barn født i omsorgsår`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(februar(2024), desember(2024)).alleMåneder(),
                pliktigEllerFrivillig = emptySet(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    setOf(
                        januar(2024).tilOmsorgsmåned(
                            DomainOmsorgstype.Barnetrygd.Full
                        )
                    )
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom alle omsorgsmåneder uten medlemskap`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), desember(2024)).alleMåneder(),
                pliktigEllerFrivillig = emptySet(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom man har ikke har tilstrekkelig antall omsorgsmåneder utover de månedene man ikke er medlem`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), april(2024)).alleMåneder(),
                pliktigEllerFrivillig = emptySet(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        januar(2024),
                        september(2024)
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom ingen omsorgsmåneder med pliktig eller frivillig medlemskap`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = emptySet(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    setOf(
                        januar(2024).tilOmsorgsmåned(
                            DomainOmsorgstype.Barnetrygd.Full
                        )
                    )
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom man har tilstrekkelig antall omsorgsmåneder utover de månedene med pliktig eller frivillig medlemskap`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = Periode(januar(2024), april(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom man har tilstrekkelig antall omsorgsmåneder utover de månedene med pliktig eller frivillig medlemskap - barn født i omsorgsår`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = Periode(februar(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    setOf(
                        januar(2024).tilOmsorgsmåned(
                            DomainOmsorgstype.Barnetrygd.Full
                        )
                    )
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `ubestemt dersom alle omsorgsmåneder pliktig eller frivillig medlemskap`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = Periode(januar(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
        }
    }

    @Test
    fun `ubestemt dersom man totalt har tilstrekkelig antall omsorgsmåneder, men ikke høyt nok antall måneder som ikke er pliktig eller frivillig`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = Periode(mai(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom man totalt har tilstrekkelig antall omsorgsmåneder, men ikke høyt nok antall måneder som ikke er pliktig eller frivillig og landstilknytning EØS`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = emptySet(),
                pliktigEllerFrivillig = Periode(mai(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, it.utfall)
        }
    }

    @Test
    fun `ubestemt dersom man har halve året som pliktig eller frivillig og halve året som ikke-medlem`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), juni(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(juli(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag dersom man har halve året som pliktig eller frivillig og halve året som ikke-medlem og landstilknytning er EØS`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), juni(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(juli(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag for kombinasjon av ikke-medlem og pliktig eller frivillig uten tilstrekkelig antall omsorgsmåneder`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), mars(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(april(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        februar(2024),
                        juni((2024))
                    ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget dersom omsorg ikke kvalifiserer for automatisk godskriving men måneder som kvalifiserer for manuell behandling er tilstrekkelig`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), mars(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(april(2024), mai(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(juni(2024), august((2024))).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full) +
                            Periode(
                                september(2024),
                                desember((2024))
                            ).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `ubestemt dersom omsorg ikke kvalifiserer for automatisk godskriving men måneder som kvalifiserer for manuell behandling er tilstrekkelig`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), mars(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(april(2024), september(2024)).alleMåneder(),
                omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(april(2024), desember((2024))).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java, it.utfall)
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

        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), juni(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(juli(2024), desember(2024)).alleMåneder(),
                omsorgsmåneder = alle,
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand),
            )
        ).also {
            assertThat(it.grunnlag.omsorgsmåneder()).isEqualTo(relevant)
        }
    }

    @Test
    fun `bruker og tar vare på relevante omsorgsmåneder ved vurdering av vilkår - kan ikke godskrives automatisk`() {
        val alle = Omsorgsmåneder.Barnetrygd(
            Periode(januar(2000), september(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Delt) +
                    Periode(oktober(2000), desember(2000)).tilOmsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
        )

        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag.new(
                ikkeMedlem = Periode(januar(2024), juni(2024)).alleMåneder(),
                pliktigEllerFrivillig = Periode(juli(2024), Companion.desember(2024)).alleMåneder(),
                omsorgsmåneder = alle,
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Eøs.UkjentPrimærOgSekundærLand),
            )
        ).also {
            assertThat(it.grunnlag.omsorgsmåneder()).isEqualTo(alle)
        }
    }
}
