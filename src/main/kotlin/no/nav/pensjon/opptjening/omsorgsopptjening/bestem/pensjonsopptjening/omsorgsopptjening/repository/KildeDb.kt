package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde


internal enum class KildeDb {
    BARNETRYGD,
    INFOTRYGD,
}

internal fun DomainKilde.toDb(): KildeDb {
    return when (this) {
        DomainKilde.BARNETRYGD -> KildeDb.BARNETRYGD
        DomainKilde.INFOTRYGD -> KildeDb.INFOTRYGD
    }
}

internal fun KildeDb.toDomain(): DomainKilde {
    return when (this) {
        KildeDb.BARNETRYGD -> DomainKilde.BARNETRYGD
        KildeDb.INFOTRYGD -> DomainKilde.INFOTRYGD
    }
}