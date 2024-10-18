package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.spring

import com.zaxxer.hikari.HikariDataSource
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingThread
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class Config {

    @Bean
    fun datasourceReadiness(
        hikariDataSource: HikariDataSource
    ): DatasourceReadinessCheck {
        return DatasourceReadinessCheck(hikariDataSource)
    }

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun threadsConfig(
        oppgaveProcessingThread: OppgaveProcessingThread,
        brevProcessingThread: BrevProcessingThread,
        persongrunnlagMeldingProcessingThread: PersongrunnlagMeldingProcessingThread,
        kontrollbehandlingProcessingThread: KontrollbehandlingProcessingThread,
        godskrivOpptjeningProcessingThread: GodskrivOpptjeningProcessingThread
    ): ExecutorService {
        return Executors.newVirtualThreadPerTaskExecutor().also {
            it.submit(oppgaveProcessingThread)
            it.submit(brevProcessingThread)
            it.submit(persongrunnlagMeldingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(kontrollbehandlingProcessingThread)
            it.submit(godskrivOpptjeningProcessingThread)
        }
    }
}
