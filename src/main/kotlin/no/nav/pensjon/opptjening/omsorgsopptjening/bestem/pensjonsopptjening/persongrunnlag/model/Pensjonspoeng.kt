package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

sealed class Pensjonspoeng : Comparable<Pensjonspoeng> {
    abstract val år: Int
    abstract val poeng: Double

    companion object {
        fun opptjenesForOmsorg(): Double = 3.5
    }

    override fun compareTo(other: Pensjonspoeng): Int {
        return poeng.compareTo(other.poeng)
    }

    data class Omsorg(
        override val år: Int,
        override val poeng: Double,
        val type: DomainOmsorgskategori
    ) : Pensjonspoeng()

    data class Inntekt(
        override val år: Int,
        override val poeng: Double,
    ) : Pensjonspoeng()
}