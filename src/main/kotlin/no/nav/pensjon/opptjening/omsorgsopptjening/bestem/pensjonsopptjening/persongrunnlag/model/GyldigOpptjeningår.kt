package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

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
    fun gyldigOpptjeningsår(): GyldigOpptjeningår {
        return GyldigOpptjeningsårImpl(2022)
    }
}