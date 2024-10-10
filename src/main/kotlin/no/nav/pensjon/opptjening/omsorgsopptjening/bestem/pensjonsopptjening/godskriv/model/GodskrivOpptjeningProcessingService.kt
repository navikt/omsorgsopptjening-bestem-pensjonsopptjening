package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

interface GodskrivOpptjeningProcessingService {
    fun process(): List<GodskrivOpptjening.Persistent>?
}