package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NavUnleashConfig {
    @Bean
    fun unleashConfig(): Unleash {
        return FakeUnleash().also { it.enableAll() }
    }
}

