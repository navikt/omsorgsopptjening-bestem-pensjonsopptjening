package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype

internal enum class OmsorgstypeDb {
    DELT_BARNETRYGD,
    FULL_BARNETRYGD,
    HJELPESTØNAD
}

internal fun DomainOmsorgstype.toDb(): OmsorgstypeDb {
    return when (this) {
        DomainOmsorgstype.Barnetrygd.Delt -> OmsorgstypeDb.DELT_BARNETRYGD
        DomainOmsorgstype.Barnetrygd.Full -> OmsorgstypeDb.FULL_BARNETRYGD
        DomainOmsorgstype.Hjelpestønad -> OmsorgstypeDb.HJELPESTØNAD
    }
}

internal fun OmsorgstypeDb.toDomain(): DomainOmsorgstype {
    return when (this) {
        OmsorgstypeDb.DELT_BARNETRYGD -> DomainOmsorgstype.Barnetrygd.Delt
        OmsorgstypeDb.FULL_BARNETRYGD -> DomainOmsorgstype.Barnetrygd.Full
        OmsorgstypeDb.HJELPESTØNAD -> DomainOmsorgstype.Hjelpestønad
    }
}