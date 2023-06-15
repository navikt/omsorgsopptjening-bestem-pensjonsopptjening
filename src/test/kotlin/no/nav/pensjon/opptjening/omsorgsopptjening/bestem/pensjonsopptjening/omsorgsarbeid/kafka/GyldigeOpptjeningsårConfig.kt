package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
internal class GyldigOpptjeningsår2020 {
    @Bean
    @Primary
    fun gyldigOpptjeningsår(): GyldigOpptjeningår {
        return GyldigOpptjeningsårImpl(listOf(2020))
    }
}

@Configuration
internal class GyldigOpptjeningsår2020Og2021 {
    @Bean
    @Primary
    fun gyldigOpptjeningsår(): GyldigOpptjeningår {
        return GyldigOpptjeningsårImpl(listOf(2020, 2021))
    }
}