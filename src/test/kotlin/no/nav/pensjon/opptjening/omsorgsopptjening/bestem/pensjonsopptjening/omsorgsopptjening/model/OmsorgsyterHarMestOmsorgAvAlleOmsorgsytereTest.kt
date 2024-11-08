package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.omsorgsmånederHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.tilOmsorgsmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.august
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OmsorgsyterHarMestOmsorgAvAlleOmsorgsytereTest {
    @Test
    fun `innvilget hvis alene om å ha flest måneder med full barnetrygd`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), juli(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(august(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isTrue()
        }
    }

    @Test
    fun `avslag hvis andre har flest måneder med full barnetrygd`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), mars(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(april(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg()).isFalse()
        }
    }

    @Test
    fun `ubestemt for hvis flere har like mange måneder full omsorg`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), juni(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(juli(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()).isTrue()
        }
    }

    @Test
    fun `ubestemt for hvis flere har like mange måneder full omsorg for hjelpestønadsmottaker`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder =
                        Periode(januar(2022), juni(2022)).omsorgsmånederHjelpestønad(DomainOmsorgstype.Barnetrygd.Full),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder =
                            Periode(juli(2022), desember(2022)).omsorgsmånederHjelpestønad(DomainOmsorgstype.Barnetrygd.Full),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java)
        }
    }

    @Test
    fun `innvilget hvis omsorgsyter har flest måneder full omsorg og andre har flere delt omsorg`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), januar(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Full
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(februar(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Innvilget::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isTrue()
        }
    }

    @Test
    fun `ubestemt hvis flere har like mange måneder med delt omsorg`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Ubestemt::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg()).isTrue()
        }
    }

    @Test
    fun `innvilget hvis man er alene om å ha flest delte måneder`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(mai(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederUavhengigAvFullEllerDelt()).isTrue()
        }
    }

    @Test
    fun `avslag hvis man ikke har flest delte måneder`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(mai(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederUavhengigAvFullEllerDelt()).isFalse()
        }
    }

    @Test
    fun `innvilget hvis kombinasjon av full og delt gir flest antall måneder`() {
        OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag(
                omsorgsyter = "mor",
                data = setOf(
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "mor",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(januar(2022), april(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            ) +
                                    Periode(mai(2022), desember(2022)).tilOmsorgsmåneder(
                                        DomainOmsorgstype.Barnetrygd.Delt
                                    )
                        ),
                        omsorgsår = 2022
                    ),
                    OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.OmsorgsmånederForMottakerOgÅr(
                        omsorgsyter = "far",
                        omsorgsmottaker = "gutt",
                        omsorgsmåneder = Omsorgsmåneder.Barnetrygd(
                            Periode(mai(2022), desember(2022)).tilOmsorgsmåneder(
                                DomainOmsorgstype.Barnetrygd.Delt
                            )
                        ),
                        omsorgsår = 2022
                    )
                )
            )
        ).let {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java)
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg()).isFalse()
            assertThat(it.grunnlag.omsorgsyterHarFlestOmsorgsmånederUavhengigAvFullEllerDelt()).isTrue()
        }
    }
}