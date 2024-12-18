package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import java.util.UUID

interface GodskrivOpptjeningService {
    fun opprett(godskrivOpptjening: GodskrivOpptjening.Transient): GodskrivOpptjening.Persistent
    fun håndter(godskrivOpptjening: GodskrivOpptjening.Persistent): GodskrivOpptjening.Persistent
    fun retry(godskrivOpptjening: GodskrivOpptjening.Persistent, ex: Throwable)
    fun stoppForMelding(meldingsId: UUID, begrunnelse: String?)
    fun stopp(id: UUID, begrunnelse: String?): UUID?
    fun restart(id: UUID): UUID?
    fun hentOgLås(antall: Int): GodskrivOpptjeningRepo.Locked
    fun frigiLås(låst: GodskrivOpptjeningRepo.Locked)
}