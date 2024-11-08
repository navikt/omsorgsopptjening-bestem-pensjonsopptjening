package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import java.time.YearMonth

internal data class OmsorgsmånederDb(
    val omsorgsmåneder: Set<OmsorgsmånedDb>,
    val type: OmsorgskategoriDb
)

internal data class OmsorgsmånedDb(
    val måned: YearMonth,
    val omsorgstypeDb: OmsorgstypeDb
)

internal fun Omsorgsmåneder.toDb(): OmsorgsmånederDb {
    return when (this) {
        is Omsorgsmåneder.Barnetrygd -> {
            OmsorgsmånederDb(omsorgsmåneder = omsorgsmåneder.toDb(), type = omsorgstype().toDb())
        }

        is Omsorgsmåneder.BarnetrygdOgHjelpestønad -> {
            OmsorgsmånederDb(omsorgsmåneder = omsorgsmåneder.toDb(), type = omsorgstype().toDb())
        }
    }
}

internal fun OmsorgsmånederDb.toDomain(): Omsorgsmåneder {
    return when (type) {
        OmsorgskategoriDb.BARNETRYGD -> {
            Omsorgsmåneder.Barnetrygd(omsorgsmåneder.toDomain())
        }

        OmsorgskategoriDb.HJELPESTØNAD -> {
            Omsorgsmåneder.BarnetrygdOgHjelpestønad(omsorgsmåneder.toDomain())
        }
    }
}

internal fun Set<OmsorgsmånedDb>.toDomain(): Set<Omsorgsmåneder.Omsorgsmåned> {
    return map { it.toDomain() }.toSet()
}

internal fun OmsorgsmånedDb.toDomain(): Omsorgsmåneder.Omsorgsmåned {
    return Omsorgsmåneder.Omsorgsmåned(måned, omsorgstypeDb.toDomain())
}

internal fun Omsorgsmåneder.Barnetrygd.toDb(): OmsorgsmånederDb {
    return OmsorgsmånederDb(omsorgsmåneder.toDb(), omsorgstype().toDb())
}

internal fun Set<Omsorgsmåneder.Omsorgsmåned>.toDb(): Set<OmsorgsmånedDb> {
    return map { it.toDb() }.toSet()
}

internal fun Omsorgsmåneder.Omsorgsmåned.toDb(): OmsorgsmånedDb {
    return OmsorgsmånedDb(måned, omsorgstype.toDb())
}