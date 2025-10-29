package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

interface GyldigOpptjeningår {
    fun erGyldig(år: Int): Boolean
}

data class GyldigOpptjeningsårImpl(
    private val år: Int
) : GyldigOpptjeningår {
    override fun erGyldig(år: Int): Boolean {
        return this.år == (år)
    }
}

@Configuration
class GyldigOpptjeningsårConfig {
    @Bean
    fun gyldigOpptjeningsår(
        @Value($$"${GYLDIG_OPPTJENINGSAR}") gyldigOpptjeningsår: String,
    ): GyldigOpptjeningår {
        return GyldigOpptjeningsårImpl(gyldigOpptjeningsår.toInt())
    }
}