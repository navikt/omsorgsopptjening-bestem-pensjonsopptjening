package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import java.time.YearMonth

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketDatagrunnlagDb")
internal data class BeriketDatagrunnlagDb(
    val omsorgsyter: PersonDb,
    val omsorgsSaker: List<BeriketOmsorgSakDb>,
    val innlesingId: InnlesingId,
    val correlationId: CorrelationId,
)

internal fun BeriketDatagrunnlag.toDb(): BeriketDatagrunnlagDb {
    return BeriketDatagrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgsSaker = omsorgsSaker.map { it.toDb() },
        innlesingId = innlesingId,
        correlationId = correlationId,
    )
}

internal fun BeriketDatagrunnlagDb.toDomain(): BeriketDatagrunnlag {
    return BeriketDatagrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsSaker = omsorgsSaker.map { it.toDomain() },
        innlesingId = innlesingId,
        correlationId = correlationId,
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
    val omsorgstype: String,
    val omsorgsmottaker: PersonDb,
    val kilde: KildeDb,
)

internal fun BeriketVedtaksperiode.toDb(): BeriketOmsorgVedtakPeriodeDb {
    return BeriketOmsorgVedtakPeriodeDb(
        fom = fom.toString(),
        tom = tom.toString(),
        omsorgstype = omsorgstype.toString(),
        omsorgsmottaker = omsorgsmottaker.toDb(),
        kilde = kilde.toDb()
    )
}

internal fun BeriketOmsorgVedtakPeriodeDb.toDomain(): BeriketVedtaksperiode {
    return BeriketVedtaksperiode(
        fom = YearMonth.parse(fom),
        tom = YearMonth.parse(tom),
        omsorgstype = DomainOmsorgstype.valueOf(omsorgstype),
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        kilde = kilde.toDomain()
    )
}
