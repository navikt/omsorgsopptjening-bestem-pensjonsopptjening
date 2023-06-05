package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype

enum class DomainOmsorgstype {
    BARNETRYGD,
}

fun Omsorgstype.toDomain(): DomainOmsorgstype {
    return when (this) {
        Omsorgstype.BARNETRYGD -> DomainOmsorgstype.BARNETRYGD
        Omsorgstype.HJELPESTØNAD_SATS_3 -> TODO()
        Omsorgstype.HJELPESTØNAD_SATS_4 -> TODO()
    }
}

fun DomainOmsorgstype.toKafka(): Omsorgstype {
    return when (this) {
        DomainOmsorgstype.BARNETRYGD -> Omsorgstype.BARNETRYGD
    }
}