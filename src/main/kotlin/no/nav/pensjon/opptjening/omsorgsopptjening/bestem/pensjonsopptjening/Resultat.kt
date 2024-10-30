package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

sealed class Resultat<T> {
    class FantIngenDataÅProsessere<T> : Resultat<T>()

    class Prosessert<T>(val data: T) : Resultat<T>()
}