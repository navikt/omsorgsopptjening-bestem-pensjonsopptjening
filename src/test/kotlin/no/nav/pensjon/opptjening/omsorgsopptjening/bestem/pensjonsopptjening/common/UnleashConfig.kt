package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!dev-gcp", "!prod-gcp")
class UnleashConfig {
    @Bean
    fun unleashConfig(): Unleash {
        val unleash = FakeUnleash()
        unleash.enableAll()
        return unleash
    }
}