package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OmsorgsyterLoveMeVurderingIFolketrygdenTest {

    @Test
    fun `omsorgsyter for barnetrygd er medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                loveMEVurdering = Medlemskapsgrunnlag.LoveMeVurdering.MEDLEM_I_FOLKETRYGDEN,
                omsorgstype = DomainOmsorgstype.BARNETRYGD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum))
            }
        }
    }

    @Test
    fun `omsorgsyter for barnetrygd er kanskje medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                loveMEVurdering = Medlemskapsgrunnlag.LoveMeVurdering.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN,
                omsorgstype = DomainOmsorgstype.BARNETRYGD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum))
            }
        }
    }

    @Test
    fun `omsorgsyter for barnetrygd er ikke medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                loveMEVurdering = Medlemskapsgrunnlag.LoveMeVurdering.IKKE_MEDLEM_I_FOLKETRYGDEN,
                omsorgstype = DomainOmsorgstype.BARNETRYGD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum))
            }
        }
    }

    @Test
    fun `omsorgsyter for hjelpestønad er medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                loveMEVurdering = Medlemskapsgrunnlag.LoveMeVurdering.MEDLEM_I_FOLKETRYGDEN,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum))
            }
        }
    }

    @Test
    fun `omsorgsyter for hjelpestønad er kanskje medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                loveMEVurdering = Medlemskapsgrunnlag.LoveMeVurdering.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum))
            }
        }
    }

    @Test
    fun `omsorgsyter for hjelpestønad er ikke medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                loveMEVurdering = Medlemskapsgrunnlag.LoveMeVurdering.IKKE_MEDLEM_I_FOLKETRYGDEN,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum))
            }
        }
    }
}