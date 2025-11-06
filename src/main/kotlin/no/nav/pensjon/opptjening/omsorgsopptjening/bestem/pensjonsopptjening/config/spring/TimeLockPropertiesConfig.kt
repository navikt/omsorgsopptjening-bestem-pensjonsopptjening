package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.TimeLock
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class TimeLockPropertiesConfig {

    @Bean
    fun timeLockProperties(
        @Value($$"${timelock.initialDelaySeconds}") initialDelaySeconds: Long,
        @Value($$"${timelock.maxDelaySeconds}") maxDelaySeconds: Long,
    ): TimeLock.Properties {
        return TimeLock.Properties(
            initialDelaySeconds = Duration.ofSeconds(initialDelaySeconds),
            maxDelaySeconds = Duration.ofSeconds(maxDelaySeconds),
        )
    }
}