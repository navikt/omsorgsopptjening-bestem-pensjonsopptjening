package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.LandstilknytningMåned
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
import java.time.YearMonth

data class LandstilknytningMånedDb(
    val måned: YearMonth,
    val landstilknytningDb: LandstilknytningDb
)

internal fun Landstilknytningmåneder.toDb(): Set<LandstilknytningMånedDb> {
    return måneder.map {
        LandstilknytningMånedDb(
            måned = it.måned,
            landstilknytningDb = it.landstilknytning.toDb()
        )
    }.toSet()
}

internal fun Set<LandstilknytningMånedDb>.toDomain(): Set<LandstilknytningMåned> {
    return map {
        LandstilknytningMåned(
            måned = it.måned,
            landstilknytning = it.landstilknytningDb.toDomain()
        )
    }.toSet()
}