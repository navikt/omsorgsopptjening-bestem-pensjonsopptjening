package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("Fiks etterhvert som vi har noe fornuftig å sjekke")
class OmsorgsyterErMedlemIFolketrygdenTest {

    @Test
    fun `omsorgsyter for barnetrygd er medlem i folketrygden`() {
        OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(
            OmsorgsyterErMedlemIFolketrygden.Grunnlag(
                medlemskapsgrunnlag = TODO(),
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
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
                medlemskapsgrunnlag = TODO(),
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
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
                medlemskapsgrunnlag = TODO(),
                omsorgstype = DomainOmsorgskategori.BARNETRYGD
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
                medlemskapsgrunnlag = TODO(),
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD
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
                medlemskapsgrunnlag = TODO(),
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD
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
                medlemskapsgrunnlag = TODO(),
                omsorgstype = DomainOmsorgskategori.HJELPESTØNAD
            )
        ).also {
            Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall).also {
                assertThat(it.henvisninger).isEqualTo(setOf(JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum))
            }
        }
    }
}