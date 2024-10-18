package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class NavUnleashConfig {
    @Bean
    fun unleashConfig(): Unleash {
        // return FakeUnleash().also { it.enableAll() }
        return FakeUnleash().also { it.disableAll() }
    }

    @Bean
    fun unleashWrapper(
        unleash: Unleash
    ): UnleashWrapper {
        return UnleashWrapper(unleash, Clock.systemUTC())
    }
}

