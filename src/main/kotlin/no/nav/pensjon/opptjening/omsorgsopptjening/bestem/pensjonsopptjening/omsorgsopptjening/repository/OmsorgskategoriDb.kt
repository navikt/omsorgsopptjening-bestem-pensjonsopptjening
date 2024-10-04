package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori

internal enum class OmsorgskategoriDb {
    BARNETRYGD,
    HJELPESTØNAD,
}

internal fun DomainOmsorgskategori.toDb(): OmsorgskategoriDb {
    return when (this) {
        DomainOmsorgskategori.BARNETRYGD -> OmsorgskategoriDb.BARNETRYGD
        DomainOmsorgskategori.HJELPESTØNAD -> OmsorgskategoriDb.HJELPESTØNAD
    }
}

internal fun OmsorgskategoriDb.toDomain(): DomainOmsorgskategori {
    return when (this) {
        OmsorgskategoriDb.BARNETRYGD -> DomainOmsorgskategori.BARNETRYGD
        OmsorgskategoriDb.HJELPESTØNAD -> DomainOmsorgskategori.HJELPESTØNAD
    }
}

