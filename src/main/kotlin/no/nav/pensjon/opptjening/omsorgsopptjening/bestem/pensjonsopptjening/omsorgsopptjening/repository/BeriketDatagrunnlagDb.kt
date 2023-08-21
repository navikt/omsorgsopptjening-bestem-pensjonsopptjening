package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import java.time.YearMonth

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketDatagrunnlagDb")
internal data class BeriketDatagrunnlagDb(
    val omsorgsyter: PersonDb,
    val omsorgstype: OmsorgstypeDb,
    val kjoreHash: String,
    val kilde: KildeDb,
    val omsorgsSaker: List<BeriketOmsorgSakDb>
)

internal fun BeriketDatagrunnlag.toDb(): BeriketDatagrunnlagDb {
    return BeriketDatagrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgstype = omsorgstype.toDb(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDb(),
        omsorgsSaker = omsorgsSaker.map { it.toDb() }
    )
}

internal fun BeriketDatagrunnlagDb.toDomain(): BeriketDatagrunnlag {
    return BeriketDatagrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgstype = omsorgstype.toDomain(),
        kjoreHash = kjoreHash,
        kilde = kilde.toDomain(),
        omsorgsSaker = omsorgsSaker.map { it.toDomain() }
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketOmsorgSakDb")
internal data class BeriketOmsorgSakDb(
    val omsorgsyter: PersonDb,
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
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketOmsorgVedtakPeriodeDb")
internal data class BeriketOmsorgVedtakPeriodeDb(
    val fom: String,
    val tom: String,
    val prosent: Int,
    val omsorgsmottaker: PersonDb
)

internal fun BeriketVedtaksperiode.toDb(): BeriketOmsorgVedtakPeriodeDb {
    return BeriketOmsorgVedtakPeriodeDb(
        fom = fom.toString(),
        tom = tom.toString(),
        prosent = prosent,
        omsorgsmottaker = PersonDb(
            fnr = omsorgsmottaker.fnr,
            fødselsdato = omsorgsmottaker.fødselsdato.toString()
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
