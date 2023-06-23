package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype

internal enum class OmsorgstypeDb {
    BARNETRYGD;
}
internal fun DomainOmsorgstype.toDb(): OmsorgstypeDb {
    return when (this) {
        DomainOmsorgstype.BARNETRYGD -> OmsorgstypeDb.BARNETRYGD
    }
}

internal fun OmsorgstypeDb.toDomain(): DomainOmsorgstype {
    return when (this) {
        OmsorgstypeDb.BARNETRYGD -> DomainOmsorgstype.BARNETRYGD
    }
}

