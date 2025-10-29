package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.OppgaveKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OppgaveConfig {

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun oppgaveProcessingThread(
        service: OppgaveService,
        unleash: UnleashWrapper,
        oppgaveProcessingMetricsMåling: OppgaveProcessingMetrikker,
        oppgaveProcessingMetricsFeilmåling: OppgaveProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): OppgaveProcessingTask {
        return OppgaveProcessingTask(
            service = service,
            unleash = unleash,
            oppgaveProcessingMetricsMåling = oppgaveProcessingMetricsMåling,
            oppgaveProcessingMetricsFeilmåling = oppgaveProcessingMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck,
        )
    }

    @Bean
    fun oppgaveService(
        sakKlient: BestemSakKlient,
        oppgaveKlient: OppgaveKlient,
        oppgaveRepo: OppgaveRepo,
        personOppslag: PersonOppslag,
        transactionTemplate: NewTransactionTemplate
    ): OppgaveService {
        return OppgaveService(
            sakKlient = sakKlient,
            oppgaveKlient = oppgaveKlient,
            oppgaveRepo = oppgaveRepo,
            personOppslag = personOppslag,
            transactionTemplate = transactionTemplate
        )
    }
}