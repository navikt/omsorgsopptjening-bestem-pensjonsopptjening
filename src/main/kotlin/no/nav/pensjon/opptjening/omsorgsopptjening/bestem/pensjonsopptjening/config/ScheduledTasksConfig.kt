package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering.StatusCheckTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering.StatusRapporteringCachingAdapter
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.tasks.FrigiLaserTask
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@Profile("dev-gcp", "prod-gcp")
class ScheduledTasksConfig {

    @Autowired
    private lateinit var statusRapporteringsService: StatusRapporteringCachingAdapter

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    @Bean
    fun statusCheckTask(): StatusCheckTask {
        return StatusCheckTask(statusRapporteringsService)
    }

    @Bean
    fun frigiLaserTask(): FrigiLaserTask {
        return FrigiLaserTask(persongrunnlagRepo, oppgaveRepo, brevRepository, godskrivOpptjeningRepo)
    }
}