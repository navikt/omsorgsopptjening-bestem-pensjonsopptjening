package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.tasks

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

class FrigiLaserTask(
    val meldingRepo: PersongrunnlagRepo,
    val oppgaveRepo: OppgaveRepo,
    val brevRepo: BrevRepository,
    val godskrivOpptjeningRepo: GodskrivOpptjeningRepo
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Scheduled(cron = "*/15 * * * * *")
    fun run() {
        try {
            log.info("Frigir gamle låser")
            meldingRepo.frigiGamleLåser()
            oppgaveRepo.frigiGamleLåser()
            brevRepo.frigiGamleLåser()
            godskrivOpptjeningRepo.frigiGamleLåser()
        } catch(ex: Throwable) {
            log.error("Feil ved frigiving av gamle låser", ex)
        }
    }
}