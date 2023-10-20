package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden

sealed class Medlemskap {

    object Ja : Medlemskap()

    object Nei : Medlemskap()

    object Ukjent : Medlemskap()

    fun erMedlem(): Boolean {
        return this is Ja || this is Ukjent
    }
}


internal fun MedlemIFolketrygden.toDomain(): Medlemskap {
    return when(this){
        MedlemIFolketrygden.Ja -> Medlemskap.Ja
        MedlemIFolketrygden.Nei -> Medlemskap.Nei
        MedlemIFolketrygden.Ukjent -> Medlemskap.Ukjent
    }
}