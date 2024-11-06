package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.YtelsePeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.YtelseType
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelsegrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelseinformasjon
import java.time.YearMonth

data class YtelsegrunnlagDb(
    val ytelser: Set<YtelseInformasjonDb>
)

data class YtelseInformasjonDb(
    val perioder: Set<YtelsePeriodeDb>,
    val rådata: String
)

data class YtelsePeriodeDb(
    val fom: YearMonth,
    val tom: YearMonth,
    val type: YtelseTypeDb,
)


enum class YtelseTypeDb {
    ALDERSPENSJON,
    UFØRETRYGD,
}

fun Ytelsegrunnlag.toDb(): YtelsegrunnlagDb {
    return YtelsegrunnlagDb(ytelser.map { it.toDb() }.toSet())
}

fun YtelsegrunnlagDb.toDomain(): Ytelsegrunnlag {
    return Ytelsegrunnlag(ytelser.map { it.toDomain() }.toSet())
}

fun Ytelseinformasjon.toDb(): YtelseInformasjonDb {
    return YtelseInformasjonDb(
        perioder = perioder.map { it.toDb() }.toSet(),
        rådata = rådata
    )
}

fun YtelseInformasjonDb.toDomain(): Ytelseinformasjon {
    return Ytelseinformasjon(
        perioder = perioder.map { it.toDomain() }.toSet(),
        rådata = rådata,
    )
}

fun YtelsePeriode.toDb(): YtelsePeriodeDb {
    return YtelsePeriodeDb(
        fom = fom,
        tom = tom,
        type = type.toDb()
    )
}

fun YtelsePeriodeDb.toDomain(): YtelsePeriode {
    return YtelsePeriode(
        fom = fom,
        tom = tom,
        type = type.toDomain()
    )
}

fun YtelseType.toDb(): YtelseTypeDb {
    return when (this) {
        YtelseType.ALDERSPENSJON -> YtelseTypeDb.ALDERSPENSJON
        YtelseType.UFØRETRYGD -> YtelseTypeDb.UFØRETRYGD
    }
}

fun YtelseTypeDb.toDomain(): YtelseType {
    return when (this) {
        YtelseTypeDb.ALDERSPENSJON -> YtelseType.ALDERSPENSJON
        YtelseTypeDb.UFØRETRYGD -> YtelseType.UFØRETRYGD
    }
}