package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype


enum class DomainOmsorgstype {
    BARNETRYGD,
    HJELPESTØNAD;
}

fun Omsorgstype.toDomain(): DomainOmsorgstype {
    return when (this) {
        Omsorgstype.DELT_BARNETRYGD -> DomainOmsorgstype.BARNETRYGD
        Omsorgstype.FULL_BARNETRYGD -> DomainOmsorgstype.BARNETRYGD
        Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3 -> DomainOmsorgstype.HJELPESTØNAD
        Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4 -> DomainOmsorgstype.HJELPESTØNAD
    }
}