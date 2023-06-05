package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import java.time.YearMonth

internal data class BeriketOmsorgsgrunnlagDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgsAr: Int,
    val omsorgstype: OmsorgstypeDb,
    val kjoreHash: String,
    val kilde: KildeDb,
    val omsorgsSaker: List<BeriketOmsorgSakDb>,
    val originaltGrunnlag: String
)

internal fun BeriketOmsorgsgrunnlag.toDb(): BeriketOmsorgsgrunnlagDb {
    return BeriketOmsorgsgrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgsAr = omsorgsAr,
        omsorgstype = omsorgstype.toDb(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDb(),
        omsorgsSaker = omsorgsSaker.map { it.toDb() },
        originaltGrunnlag = originaltGrunnlag.mapToJson()
    )
}

internal fun BeriketOmsorgsgrunnlagDb.toDomain(): BeriketOmsorgsgrunnlag {
    return BeriketOmsorgsgrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsAr = omsorgsAr,
        omsorgstype = omsorgstype.toDomain(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDomain(),
        omsorgsSaker = omsorgsSaker.map { it.toDomain() },
        originaltGrunnlag = originaltGrunnlag.mapToClass(OmsorgsGrunnlag::class.java)
    )
}

internal data class BeriketOmsorgSakDb(
    val omsorgsyter: PersonMedFødselsårDb,
    val omsorgVedtakPeriode: List<BeriketOmsorgVedtakPeriodeDb>
)

internal fun BeriketOmsorgSak.toDb(): BeriketOmsorgSakDb {
    return BeriketOmsorgSakDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgVedtakPeriode = omsorgVedtakPeriode.map { it.toDb() }
    )
}

internal fun BeriketOmsorgSakDb.toDomain(): BeriketOmsorgSak {
    return BeriketOmsorgSak(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgVedtakPeriode = omsorgVedtakPeriode.map { it.toDomain() }
    )
}

internal data class BeriketOmsorgVedtakPeriodeDb(
    val fom: String,
    val tom: String,
    val prosent: Int,
    val omsorgsmottaker: PersonMedFødselsårDb
)

internal fun BeriketOmsorgVedtakPeriode.toDb(): BeriketOmsorgVedtakPeriodeDb {
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

internal fun BeriketOmsorgVedtakPeriodeDb.toDomain(): BeriketOmsorgVedtakPeriode {
    return BeriketOmsorgVedtakPeriode(
        fom = YearMonth.parse(fom),
        tom = YearMonth.parse(tom),
        prosent = prosent,
        omsorgsmottaker = omsorgsmottaker.toDomain()
    )
}
