package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo.Locked
import java.util.UUID

interface PersongrunnlagMeldingService {
    fun behandle(melding: PersongrunnlagMelding.Mottatt): FullførteBehandlinger
    fun retry(melding: PersongrunnlagMelding.Mottatt, ex: Throwable)
    fun hentOgLås(antall: Int): Locked
    fun avsluttMelding(id: UUID, melding: String): UUID?
    fun rekjørStoppetMelding(meldingsId: UUID): UUID?
    fun stoppOgOpprettKopiAvMelding(meldingId: UUID, begrunnelse: String?): UUID?
    fun stoppMelding(id: UUID, begrunnelse: String?): UUID?
    fun frigi(locked: Locked)
}