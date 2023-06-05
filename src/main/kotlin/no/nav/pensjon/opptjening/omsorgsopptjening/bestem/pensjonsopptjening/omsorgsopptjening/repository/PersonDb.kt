package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

internal data class PersonMedFødselsårDb(
    val fnr: String,
    val fødselsår: Int
)

internal fun PersonMedFødselsår.toDb(): PersonMedFødselsårDb {
    return PersonMedFødselsårDb(
        fnr = fnr,
        fødselsår = fodselsAr
    )
}

internal fun PersonMedFødselsårDb.toDomain(): PersonMedFødselsår {
    return PersonMedFødselsår(
        fnr = fnr,
        fodselsAr = fødselsår
    )
}
