package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.spring

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.OmsorgsopptjeningsgrunnlagService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.OmsorgsopptjeningsgrunnlagServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class PersongrunnlagConfig {

    @Bean
    fun persongrunnlagMeldingService(
        behandlingRepo: BehandlingRepo,
        gyldigOpptjeningsår: GyldigOpptjeningår,
        persongrunnlagRepo: PersongrunnlagRepo,
        oppgaveService: OppgaveService,
        personOppslag: PersonOppslag,
        godskrivOpptjeningService: GodskrivOpptjeningService,
        transactionTemplate: NewTransactionTemplate,
        brevService: BrevService,
        medlemskapsUnntakOppslag: MedlemskapsUnntakOppslag,
        omsorgsopptjeningsgrunnlagService: OmsorgsopptjeningsgrunnlagService,
    ): PersongrunnlagMeldingService {
        return PersongrunnlagMeldingServiceImpl(
            behandlingRepo = behandlingRepo,
            gyldigOpptjeningsår = gyldigOpptjeningsår,
            persongrunnlagRepo = persongrunnlagRepo,
            oppgaveService = oppgaveService,
            godskrivOpptjeningService = godskrivOpptjeningService,
            transactionTemplate = transactionTemplate,
            brevService = brevService,
            omsorgsopptjeningsgrunnlagService = omsorgsopptjeningsgrunnlagService
        )
    }

    @Bean
    fun persongrunnlgProcessingService(
        transactionTemplate: NewTransactionTemplate,
        service: PersongrunnlagMeldingService,
    ): PersongrunnlagMeldingProcessingService {
        return PersongrunnlagMeldingProcessingServiceImpl(
            transactionTemplate = transactionTemplate,
            service = service
        )
    }

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun persongrunnlagProcessingThread(
        service: PersongrunnlagMeldingProcessingService,
        unleash: Unleash,
        omsorgsarbeidMetricsMåling: OmsorgsarbeidProcessingMetrikker,
        omsorgsarbeidMetricsFeilmåling: OmsorgsarbeidProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): PersongrunnlagMeldingProcessingThread {
        return PersongrunnlagMeldingProcessingThread(
            service = service,
            unleash = unleash,
            omsorgsarbeidMetricsMåling = omsorgsarbeidMetricsMåling,
            omsorgsarbeidMetricsFeilmåling = omsorgsarbeidMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck
        )

    }

    @Bean
    fun omsorgsopptjeningsgrunnlagService(
        personOppslag: PersonOppslag,
        medlemskapsUnntakOppslag: MedlemskapsUnntakOppslag
    ): OmsorgsopptjeningsgrunnlagService {
        return OmsorgsopptjeningsgrunnlagServiceImpl(
            personOppslag = personOppslag,
            medlemskapsUnntakOppslag = medlemskapsUnntakOppslag
        )
    }

}