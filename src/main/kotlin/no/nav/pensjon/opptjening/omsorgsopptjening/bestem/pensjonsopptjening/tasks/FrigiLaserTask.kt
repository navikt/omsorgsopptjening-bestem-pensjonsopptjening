package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.tasks

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling.KontrollbehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

class FrigiLaserTask(
    val meldingRepo: PersongrunnlagRepo,
    val oppgaveRepo: OppgaveRepo,
    val brevRepo: BrevRepository,
    val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
    val kontrollbehandlingRepo: KontrollbehandlingRepo,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Scheduled(cron = "0 * * * * *")
    fun run() {
        try {
            val antallMeldinger = meldingRepo.frigiGamleLåser()
            val antallOppgaver = oppgaveRepo.frigiGamleLåser()
            val antallBrev = brevRepo.frigiGamleLåser()
            val antallGodskrivinger = godskrivOpptjeningRepo.frigiGamleLåser()
            val antallKontroll = kontrollbehandlingRepo.frigiGamleLåser()
            if (antallMeldinger + antallOppgaver + antallBrev + antallGodskrivinger + antallKontroll > 0) {
                log.info("Frigjorde gamle låser: meldinger:$antallMeldinger oppgaver:$antallOppgaver brev:$antallBrev godskrivinger:$antallGodskrivinger kontroll:$antallKontroll")
            }
        } catch (ex: Throwable) {
            log.error("Feil ved frigiving av gamle låser", ex)
        }
    }
}