package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.spring

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.OmsorgsopptjeningsgrunnlagService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Clock

@Configuration
class KontrollConfig {

    @Bean
    fun kontrollService(
        omsorgsopptjeningsgrunnlagService: OmsorgsopptjeningsgrunnlagService,
        kontrollbehandlingRepo: KontrollbehandlingRepo,
        behandlingRepo: BehandlingRepo,
        persongrunnlagRepo: PersongrunnlagRepo,
        hentPensjonspoengClient: HentPensjonspoengClient,
    ): KontrollbehandlingService {
        return KontrollbehandlingServiceImpl(
            omsorgsopptjeningsgrunnlagService = omsorgsopptjeningsgrunnlagService,
            kontrollbehandlingRepo = kontrollbehandlingRepo,
            persongrunnlagRepo = persongrunnlagRepo,
            hentPensjonspoeng = hentPensjonspoengClient,
        )
    }

    @Bean
    fun kontrollProcessingService(
        transactionTemplate: NewTransactionTemplate,
        kontrollbehandlingService: KontrollbehandlingService,
    ): KontrollbehandlingProcessingService {
        return KontrollbehandlingProcessingServiceImpl(
            transactionTemplate = transactionTemplate,
            service = kontrollbehandlingService
        )
    }

    @Bean
    fun kontrollRepo(
        jdbcTemplate: NamedParameterJdbcTemplate,
    ): KontrollbehandlingRepo {
        return KontrollbehandlingRepo(
            jdbcTemplate = jdbcTemplate,
            clock = Clock.systemUTC(),
        )
    }

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun kontrollProcessingThread(
        service: KontrollbehandlingProcessingService,
        unleash: Unleash,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): KontrollbehandlingProcessingThread {
        return KontrollbehandlingProcessingThread(
            service = service,
            unleash = unleash,
            datasourceReadinessCheck = datasourceReadinessCheck
        )
    }
}