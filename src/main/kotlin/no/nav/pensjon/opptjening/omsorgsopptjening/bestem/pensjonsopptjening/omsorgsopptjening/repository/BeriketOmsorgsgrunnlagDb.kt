package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import java.time.YearMonth

internal data class BeriketOmsorgsgrunnlagDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgstype: OmsorgstypeDb,
    val kjoreHash: String,
    val kilde: KildeDb,
    val omsorgsSaker: List<BeriketOmsorgSakDb>,
    val originaltGrunnlag: String
)

internal fun BeriketDatagrunnlag.toDb(): BeriketOmsorgsgrunnlagDb {
    return BeriketOmsorgsgrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgstype = omsorgstype.toDb(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDb(),
        omsorgsSaker = omsorgsSaker.map { it.toDb() },
        originaltGrunnlag = originaltGrunnlag
    )
}

internal fun BeriketOmsorgsgrunnlagDb.toDomain(): BeriketDatagrunnlag {
    return BeriketDatagrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgstype = omsorgstype.toDomain(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDomain(),
        omsorgsSaker = omsorgsSaker.map { it.toDomain() },
        originaltGrunnlag = originaltGrunnlag
    )
}

internal data class BeriketOmsorgSakDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgVedtakPeriode: List<BeriketOmsorgVedtakPeriodeDb>
)

internal fun BeriketSak.toDb(): BeriketOmsorgSakDb {
    return BeriketOmsorgSakDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgVedtakPeriode = omsorgVedtakPerioder.map { it.toDb() }
    )
}

internal fun BeriketOmsorgSakDb.toDomain(): BeriketSak {
    return BeriketSak(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgVedtakPerioder = omsorgVedtakPeriode.map { it.toDomain() }
    )
}

internal data class BeriketOmsorgVedtakPeriodeDb(
    val fom: String,
    val tom: String,
    val prosent: Int,
    val omsorgsmottaker: PersonMedFødselsårDb
)

internal fun BeriketVedtaksperiode.toDb(): BeriketOmsorgVedtakPeriodeDb {
    return BeriketOmsorgVedtakPeriodeDb(
        fom = fom.toString(),
        tom = tom.toString(),
        prosent = prosent,
        omsorgsmottaker = PersonMedFødselsårDb(
            fnr = omsorgsmottaker.fnr,
            fødselsår = omsorgsmottaker.fodselsAr
        )
    )
}

internal fun BeriketOmsorgVedtakPeriodeDb.toDomain(): BeriketVedtaksperiode {
    return BeriketVedtaksperiode(
        fom = YearMonth.parse(fom),
        tom = YearMonth.parse(tom),
        prosent = prosent,
        omsorgsmottaker = omsorgsmottaker.toDomain()
    )
}
