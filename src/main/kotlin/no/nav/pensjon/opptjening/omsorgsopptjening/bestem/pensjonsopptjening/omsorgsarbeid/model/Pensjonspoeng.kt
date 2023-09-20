package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

sealed class Pensjonspoeng : Comparable<Pensjonspoeng> {
    abstract val år: Int
    abstract val poeng: Double
    override fun compareTo(other: Pensjonspoeng): Int {
        return poeng.compareTo(other.poeng)
    }

    data class Omsorg(
        override val år: Int,
        override val poeng: Double,
        val type: DomainOmsorgstype
    ) : Pensjonspoeng()

    data class Inntekt(
        override val år: Int,
        override val poeng: Double,
    ) : Pensjonspoeng()
}