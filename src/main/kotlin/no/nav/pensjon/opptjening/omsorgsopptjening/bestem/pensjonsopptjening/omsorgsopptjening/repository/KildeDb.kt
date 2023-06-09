package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde


internal enum class KildeDb {
    BARNETRYGD,
}

internal fun DomainKilde.toDb(): KildeDb {
    return when (this) {
        DomainKilde.BARNETRYGD -> KildeDb.BARNETRYGD
        DomainKilde.INFOTRYGD -> TODO()
    }
}

internal fun KildeDb.toDomain(): DomainKilde {
    return when (this) {
        KildeDb.BARNETRYGD -> DomainKilde.BARNETRYGD
    }
}