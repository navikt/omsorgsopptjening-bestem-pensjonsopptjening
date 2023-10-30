package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde


internal enum class KildeDb {
    BARNETRYGD,
    INFOTRYGD,
    INFOTRYGD_UTTREKK_PENSJONSTRYGDET,
}

internal fun DomainKilde.toDb(): KildeDb {
    return when (this) {
        DomainKilde.BARNETRYGD -> KildeDb.BARNETRYGD
        DomainKilde.INFOTRYGD -> KildeDb.INFOTRYGD
        DomainKilde.INFOTRYGD_UTTREKK_PENSJONSTRYGDET -> KildeDb.INFOTRYGD_UTTREKK_PENSJONSTRYGDET
    }
}

internal fun KildeDb.toDomain(): DomainKilde {
    return when (this) {
        KildeDb.BARNETRYGD -> DomainKilde.BARNETRYGD
        KildeDb.INFOTRYGD -> DomainKilde.INFOTRYGD
        KildeDb.INFOTRYGD_UTTREKK_PENSJONSTRYGDET -> DomainKilde.INFOTRYGD_UTTREKK_PENSJONSTRYGDET
    }
}