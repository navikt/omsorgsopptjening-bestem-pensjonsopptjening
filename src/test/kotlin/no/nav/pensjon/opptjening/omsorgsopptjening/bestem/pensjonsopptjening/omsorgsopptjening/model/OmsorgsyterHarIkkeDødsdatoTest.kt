package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OmsorgsyterHarIkkeDødsdatoTest {
    @Test
    fun `innvilget hvis omsorgsyter ikke har dødsdato`() {
        OmsorgsyterHarIkkeDødsdato.vilkarsVurder(
            OmsorgsyterHarIkkeDødsdato.Grunnlag(
                dødsdato = null
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `avslag hvis omsorgsyter har dødsdato`() {
        OmsorgsyterHarIkkeDødsdato.vilkarsVurder(
            OmsorgsyterHarIkkeDødsdato.Grunnlag(
                dødsdato = LocalDate.now().minusDays(1)
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall)
        }
    }
}