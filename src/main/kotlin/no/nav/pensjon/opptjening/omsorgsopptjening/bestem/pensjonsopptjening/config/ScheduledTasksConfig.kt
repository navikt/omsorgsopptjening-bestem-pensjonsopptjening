package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering.StatusCheckTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering.StatusRapporteringCachingAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
// @Profile("dev-gcp", "prod-gcp")
class ScheduledTasksConfig {

    @Autowired
    private lateinit var statusRapporteringsService: StatusRapporteringCachingAdapter
    @Bean
    fun statusCheckTask() : StatusCheckTask {
        return StatusCheckTask(statusRapporteringsService)
    }
}