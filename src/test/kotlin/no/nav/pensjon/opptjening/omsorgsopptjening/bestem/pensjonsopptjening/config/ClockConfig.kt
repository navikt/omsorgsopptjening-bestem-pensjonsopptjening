package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.TestKlokke
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ClockConfig {

    @Bean
    fun testClock(): Clock {
        return TestKlokke()
    }
}