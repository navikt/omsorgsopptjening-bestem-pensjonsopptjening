package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics

interface Metrikker<R> {
    fun oppdater(lambda: () -> R): R
}