package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import java.time.YearMonth

internal data class OmsorgsmånederDb(
    val omsorgsmåneder: Set<YearMonth>,
    val type: OmsorgskategoriDb,
)

internal fun Omsorgsmåneder.toDb(): OmsorgsmånederDb {
    return when (this) {
        is Omsorgsmåneder.Barnetrygd -> {
            OmsorgsmånederDb(this.alle(), OmsorgskategoriDb.BARNETRYGD)
        }

        is Omsorgsmåneder.Hjelpestønad -> {
            OmsorgsmånederDb(this.alle(), OmsorgskategoriDb.HJELPESTØNAD)
        }
    }
}

internal fun OmsorgsmånederDb.toDomain(): Omsorgsmåneder {
    return when (this.type) {
        OmsorgskategoriDb.BARNETRYGD -> Omsorgsmåneder.Barnetrygd(this.omsorgsmåneder)
        OmsorgskategoriDb.HJELPESTØNAD -> Omsorgsmåneder.Hjelpestønad(this.omsorgsmåneder)
    }
}