package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId

interface KontrollbehandlingService {
    fun behandle(kontrollbehandling: Kontrollbehandling): FullførteBehandlinger
    fun retry(kontrollbehandling: Kontrollbehandling, ex: Throwable)
    fun hentOgLås(antall: Int): KontrollbehandlingRepo.Locked
    fun frigi(locked: KontrollbehandlingRepo.Locked)
    fun kontrollbehandling(innlesingId: InnlesingId, referanse: String, år: Int)
}