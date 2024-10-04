package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype


enum class DomainOmsorgskategori {
    BARNETRYGD,
    HJELPESTØNAD;
}

fun Omsorgstype.toDomain(): DomainOmsorgstype {
    return when (this) {
        Omsorgstype.DELT_BARNETRYGD -> DomainOmsorgstype.Barnetrygd.Delt
        Omsorgstype.FULL_BARNETRYGD -> DomainOmsorgstype.Barnetrygd.Full
        Omsorgstype.USIKKER_BARNETRYGD -> DomainOmsorgstype.Barnetrygd.Full
        Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3 -> DomainOmsorgstype.Hjelpestønad
        Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4 -> DomainOmsorgstype.Hjelpestønad
    }
}

sealed class DomainOmsorgstype {

    abstract fun omsorgskategori(): DomainOmsorgskategori

    sealed class Barnetrygd : DomainOmsorgstype() {

        override fun omsorgskategori(): DomainOmsorgskategori {
            return DomainOmsorgskategori.BARNETRYGD
        }

        data object Full : Barnetrygd()
        data object Delt : Barnetrygd()
    }

    data object Hjelpestønad : DomainOmsorgstype(){
        override fun omsorgskategori(): DomainOmsorgskategori {
            return DomainOmsorgskategori.HJELPESTØNAD
        }
    }
}