package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.YearMonth

data class SelvstendigRettMåneder(
    private val m: Set<SelvstendigRettMåned>
) {
    val måneder get() = m.toSortedSet()

    infix fun merge(other: SelvstendigRettMåneder): SelvstendigRettMåneder {
        return SelvstendigRettMåneder((måneder + other.måneder).toSet())
    }

    fun alle(): Set<YearMonth> {
        return måneder.map { it.måned }.toSet()
    }

    fun antall(): Int {
        return alle().count()
    }

    companion object {
        fun none() = SelvstendigRettMåneder(emptySet())
    }
}

/**
 * En måned hvor det er lagt til grunn at bruker har selvstendig rett
 */
data class SelvstendigRettMåned(
    val måned: YearMonth,
) : Comparable<SelvstendigRettMåned> {

    companion object {
        fun of(måned: YearMonth, selvstendigRett: Boolean): SelvstendigRettMåned? {
            return when (selvstendigRett) {
                true -> SelvstendigRettMåned(måned)
                false -> null
            }
        }
    }

    override fun compareTo(other: SelvstendigRettMåned): Int {
        return måned.compareTo(other.måned)
    }
}


