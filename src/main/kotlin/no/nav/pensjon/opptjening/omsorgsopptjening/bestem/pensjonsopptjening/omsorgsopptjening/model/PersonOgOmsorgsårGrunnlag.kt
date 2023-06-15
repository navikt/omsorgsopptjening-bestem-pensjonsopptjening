package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

data class PersonOgOmsorgsårGrunnlag(
    val person: PersonMedFødselsår,
    val omsorgsAr: Int
) {
    fun alderMottaker(mellom: IntRange): Boolean {
        return person.alderVedUtløpAv(omsorgsAr) in mellom
    }
}