package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat

interface GodskrivOpptjeningProcessingService {
    fun process(): Resultat<List<GodskrivOpptjening.Persistent>>
}