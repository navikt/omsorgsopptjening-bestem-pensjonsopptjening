package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.util.UUID

interface KontrollbehandlingService {
    fun behandle(kontrollrad: Kontrollbehandling): FullførteBehandlinger
    fun retry(kontrollrad: Kontrollbehandling, ex: Throwable)
    fun hentOgLås(antall: Int): KontrollbehandlingRepo.Locked
    fun frigi(locked: KontrollbehandlingRepo.Locked)
    fun kontrollbehandling(innlesingId: InnlesingId, referanse: String, år: Int)
}