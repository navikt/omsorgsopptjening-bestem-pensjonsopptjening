package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

sealed class Medlemskap {

    abstract val kilde: DomainKilde

    data class Ja(
        override val kilde: DomainKilde
    ) : Medlemskap()

    data class Nei(
        override val kilde: DomainKilde
    ) : Medlemskap()

    data class Ukjent(
        override val kilde: DomainKilde,
    ) : Medlemskap()

    /**
     * Tidligere har man godtatt at verdien av "pensjonstrygdet" i Infotrygd var enten fraværende eller ja.
     */
    fun erMedlem(): Boolean {
        return this is Ja || this is Ukjent
    }
}