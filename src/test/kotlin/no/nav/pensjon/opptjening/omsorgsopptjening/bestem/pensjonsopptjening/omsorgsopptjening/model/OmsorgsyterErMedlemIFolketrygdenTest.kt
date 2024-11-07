package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.september
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.april
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mars
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OmsorgsyterErMedlemIFolketrygdenTest {

    @Test
    fun `innvilget dersom ingen omsorgsmåneder uten medlemskap`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(setOf(januar(2024).omsorgsmåned(DomainOmsorgstype.Barnetrygd.Full))),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = april(2024),
                            )
                        ),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = februar(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(setOf(januar(2024).omsorgsmåned(DomainOmsorgstype.Barnetrygd.Full))),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = april(2024),
                            )
                        ),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        januar(2024),
                        september(2024)
                    ).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(setOf(januar(2024).omsorgsmåned(DomainOmsorgstype.Barnetrygd.Full))),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = april(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = februar(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(setOf(januar(2024).omsorgsmåned(DomainOmsorgstype.Barnetrygd.Full))),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = mai(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = mai(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = juni(2024),
                            )
                        ),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = juli(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = juni(2024),
                            )
                        ),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = juli(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(år(2024).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)),
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
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = Medlemskapsgrunnlag(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = januar(2024),
                                tilOgMed = mars(2024),
                            )
                        ),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = april(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = ""
                    )
                ),
                omsorgsytersOmsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                    Periode(
                        februar(2024),
                        juni((2024))
                    ).omsorgsmåneder(DomainOmsorgstype.Barnetrygd.Full)
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                landstilknytningMåneder = år(2024).landstilknytningmåneder(Landstilknytning.Norge),
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall)
        }
    }
}
