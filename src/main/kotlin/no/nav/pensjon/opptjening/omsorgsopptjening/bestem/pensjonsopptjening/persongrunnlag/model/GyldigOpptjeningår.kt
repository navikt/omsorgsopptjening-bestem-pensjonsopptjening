package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

interface GyldigOpptjeningår {
    fun erGyldig(år: Int): Boolean
}

data class GyldigOpptjeningsårImpl(
    private val år: List<Int>
) : GyldigOpptjeningår {
    override fun erGyldig(år: Int): Boolean {
        return this.år.contains(år)
    }
}

@Configuration
class GyldigOpptjeningsårConfig {
    @Bean
    fun gyldigOpptjeningsår(): GyldigOpptjeningår {
        return GyldigOpptjeningsårImpl(listOf(2022))
    }
}