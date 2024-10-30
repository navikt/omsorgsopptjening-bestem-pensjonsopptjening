package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingTask
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class TaskConfig {

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun taskExecutorService(
        oppgaveProcessingTask: OppgaveProcessingTask,
        brevProcessingTask: BrevProcessingTask,
        persongrunnlagMeldingProcessingTask: PersongrunnlagMeldingProcessingTask,
        kontrollbehandlingProcessingTask: KontrollbehandlingProcessingTask,
        godskrivOpptjeningProcessingTask: GodskrivOpptjeningProcessingTask
    ): ExecutorService {
        return Executors.newVirtualThreadPerTaskExecutor().also { executor ->
            repeat(1) { executor.submit(oppgaveProcessingTask) }
            repeat(1) { executor.submit(brevProcessingTask) }
            repeat(4) { executor.submit(persongrunnlagMeldingProcessingTask) }
            repeat(16) { executor.submit(kontrollbehandlingProcessingTask) }
            repeat(1) { executor.submit(godskrivOpptjeningProcessingTask) }
        }
    }
}