package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class OmsorgsyterMottarBarnetrgydTest {

    @Test
    fun `henviser til riktig paragraf for barnetrygd og regel for født omsorgsår`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum))
            }
        }
    }

    @Test
    fun `henviser til riktig paragraf for barnetrygd og regel for født utenfor omsorgsår`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum))
            }
        }
    }

    @Test
    fun `henviser til riktig paragraf for hjelpestønad og regel for født utenfor omsorgsår`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_11_Andre_Ledd_Første_Punktum))
            }
        }
    }

    @Test
    fun `ikke mottatt barnetrygd i noen måneder gir avlsag for barnetrygd`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java)
        }

        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(emptySet()),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java)
        }
    }

    @Test
    fun `mottatt barnetrygd i minst 1 mnd gir innvilget ved regel for født omsorgsår`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(
                    setOf(
                        Utbetalingsmåned(
                            måned = januar(år = 2022),
                            utbetalt = 500,
                            landstilknytning = Landstilknytning.Norge
                        )
                    )
                ),
                antallMånederRegel = AntallMånederRegel.FødtIOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java)
        }
    }

    @Test
    fun `mottatt barnetrygd i minst 5 mnd gir avslag ved regel for ikke født omsorgsår`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(
                    setOf(
                        januar(2022), februar(2022), mars(2022), april(2022), mai(2022)
                    ).map {
                        Utbetalingsmåned(
                            måned = it,
                            utbetalt = 500,
                            landstilknytning = Landstilknytning.Norge
                        )
                    }.toSet()
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java)
        }
    }

    @Test
    fun `mottatt barnetrygd i minst 6 mnd gir innvilget ved regel for ikke født omsorgsår`() {
        OmsorgsyterMottarBarnetrgyd.vilkarsVurder(
            grunnlag = OmsorgsyterMottarBarnetrgyd.Grunnlag(
                omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(
                    setOf(
                        januar(2022), februar(2022), mars(2022), april(2022), mai(2022), juni(2022)
                    ).map {
                        Utbetalingsmåned(
                            måned = it,
                            utbetalt = 500,
                            landstilknytning = Landstilknytning.Norge
                        )
                    }.toSet()
                ),
                antallMånederRegel = AntallMånederRegel.FødtUtenforOmsorgsår,
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
            )
        ).also {
            assertThat(it.utfall).isInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java)
        }
    }
}