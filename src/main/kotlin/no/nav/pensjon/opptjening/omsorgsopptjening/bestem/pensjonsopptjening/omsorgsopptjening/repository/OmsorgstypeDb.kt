package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype

internal enum class OmsorgstypeDb {
    BARNETRYGD,
    HJELPESTØNAD,
}

internal fun DomainOmsorgstype.toDb(): OmsorgstypeDb {
    return when (this) {
        DomainOmsorgstype.BARNETRYGD -> OmsorgstypeDb.BARNETRYGD
        DomainOmsorgstype.HJELPESTØNAD -> OmsorgstypeDb.HJELPESTØNAD
    }
}

internal fun OmsorgstypeDb.toDomain(): DomainOmsorgstype {
    return when (this) {
        OmsorgstypeDb.BARNETRYGD -> DomainOmsorgstype.BARNETRYGD
        OmsorgstypeDb.HJELPESTØNAD -> DomainOmsorgstype.HJELPESTØNAD
    }
}

