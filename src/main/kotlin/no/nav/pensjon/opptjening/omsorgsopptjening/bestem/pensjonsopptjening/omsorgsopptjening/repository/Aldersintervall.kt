package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

data class Aldersintervall(
    val min: Int,
    val max: Int
)

internal fun IntRange.toDb(): Aldersintervall {
    return Aldersintervall(min = first, max = last)
}

internal fun Aldersintervall.toDomain(): IntRange {
    return min..max
}
