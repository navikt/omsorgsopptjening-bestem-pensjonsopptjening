package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde


enum class DomainKilde {
    BARNETRYGD,
    INFOTRYGD,
}
fun Kilde.toDomain(): DomainKilde {
    return when (this) {
        Kilde.BARNETRYGD -> DomainKilde.BARNETRYGD
        Kilde.INFOTRYGD -> DomainKilde.INFOTRYGD
    }
}