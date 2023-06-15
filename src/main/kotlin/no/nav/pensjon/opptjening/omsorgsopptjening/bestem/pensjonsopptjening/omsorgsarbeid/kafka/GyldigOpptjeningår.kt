package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

interface GyldigOpptjeningår {
    fun get(): List<Int>
}

data class GyldigOpptjeningsårImpl(
    private val år: List<Int>
) : GyldigOpptjeningår {
    override fun get(): List<Int> {
        return år
    }
}

@Configuration
class GyldigOpptjeningsårConfig {
    @Bean
    fun gyldigOpptjeningsår(): GyldigOpptjeningår {
        return GyldigOpptjeningsårImpl(listOf(2022))
    }
}