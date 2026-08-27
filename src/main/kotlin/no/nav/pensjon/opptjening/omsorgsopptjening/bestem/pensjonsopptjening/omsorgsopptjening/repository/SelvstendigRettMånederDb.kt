package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.SelvstendigRettMåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.SelvstendigRettMåneder
import java.time.YearMonth

internal data class SelvstendigRettMånederDb(
    val måneder: Set<SelvstendigRettMånedDb>,
)

internal data class SelvstendigRettMånedDb(
    val måned: YearMonth,
)

internal fun SelvstendigRettMåneder.toDb(): SelvstendigRettMånederDb {
    return SelvstendigRettMånederDb(måneder = måneder.toDb())
}

internal fun SelvstendigRettMånederDb.toDomain(): SelvstendigRettMåneder {
    return SelvstendigRettMåneder(m = måneder.toDomain())
}

internal fun Set<SelvstendigRettMånedDb>.toDomain(): Set<SelvstendigRettMåned> {
    return map { it.toDomain() }.toSet()
}

internal fun SelvstendigRettMånedDb.toDomain(): SelvstendigRettMåned {
    return SelvstendigRettMåned(måned)
}

internal fun Set<SelvstendigRettMåned>.toDb(): Set<SelvstendigRettMånedDb> {
    return map { it.toDb() }.toSet()
}

internal fun SelvstendigRettMåned.toDb(): SelvstendigRettMånedDb {
    return SelvstendigRettMånedDb(måned)
}