package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics

interface MetricsMåling<R> {
    fun mål(lambda: () -> R): R
}

interface MetricsFeilmåling<T> {
    fun målfeil(lambda: () -> T)
}