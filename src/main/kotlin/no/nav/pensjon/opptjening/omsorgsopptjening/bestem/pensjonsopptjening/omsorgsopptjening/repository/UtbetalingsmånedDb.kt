package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåned
import java.time.YearMonth

data class UtbetalingsmånedDb(
    val måned: YearMonth,
    val utbetalt: Int,
    val landstilknytning: LandstilknytningDb
)

fun Utbetalingsmåned.toDb(): UtbetalingsmånedDb {
    return UtbetalingsmånedDb(
        måned = måned,
        utbetalt = utbetalt,
        landstilknytning = landstilknytning.toDb(),
    )
}

fun UtbetalingsmånedDb.toDomain(): Utbetalingsmåned {
    return Utbetalingsmåned(
        måned = måned,
        utbetalt = utbetalt,
        landstilknytning = landstilknytning.toDomain(),
    )
}