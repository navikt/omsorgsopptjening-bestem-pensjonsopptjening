package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Omsorgsmåneder
import java.time.YearMonth

internal data class OmsorgsmånederDb(
    val omsorgsmåneder: Set<YearMonth>,
    val type: OmsorgstypeDb,
)

internal fun Omsorgsmåneder.toDb(): OmsorgsmånederDb {
    return when (this) {
        is Omsorgsmåneder.Barnetrygd -> {
            OmsorgsmånederDb(this.alle(), OmsorgstypeDb.BARNETRYGD)
        }

        is Omsorgsmåneder.Hjelpestønad -> {
            OmsorgsmånederDb(this.alle(), OmsorgstypeDb.HJELPESTØNAD)
        }
    }
}

internal fun OmsorgsmånederDb.toDomain(): Omsorgsmåneder {
    return when (this.type) {
        OmsorgstypeDb.BARNETRYGD -> Omsorgsmåneder.Barnetrygd(this.omsorgsmåneder)
        OmsorgstypeDb.HJELPESTØNAD -> Omsorgsmåneder.Hjelpestønad(this.omsorgsmåneder)
    }
}