package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Hjelpestønadperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Persongrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.YearMonth

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketDatagrunnlagDb")
internal data class BeriketDatagrunnlagDb(
    val omsorgsyter: PersonDb,
    val persongrunnlag: List<BeriketPersongrunnlagDb>,
    val innlesingId: InnlesingId,
    val correlationId: CorrelationId,
)

internal fun BeriketDatagrunnlag.toDb(): BeriketDatagrunnlagDb {
    return BeriketDatagrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        persongrunnlag = persongrunnlag.map { it.toDb() },
        innlesingId = innlesingId,
        correlationId = correlationId,
    )
}

internal fun BeriketDatagrunnlagDb.toDomain(): BeriketDatagrunnlag {
    return BeriketDatagrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        persongrunnlag = persongrunnlag.map { it.toDomain() },
        innlesingId = innlesingId,
        correlationId = correlationId,
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketPersongrunnlagDb")
internal data class BeriketPersongrunnlagDb(
    val omsorgsyter: PersonDb,
    val omsorgVedtakPeriode: List<BeriketOmsorgsperiodeDb>,
    val hjelpestønadperioder: List<BeriketOmsorgsperiodeHjelpestønad>,
)

internal fun Persongrunnlag.toDb(): BeriketPersongrunnlagDb {
    return BeriketPersongrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgVedtakPeriode = omsorgsperioder.map { it.toDb() },
        hjelpestønadperioder = hjelpestønadperioder.map { it.toDb() }
    )
}

internal fun BeriketPersongrunnlagDb.toDomain(): Persongrunnlag {
    return Persongrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsperioder = omsorgVedtakPeriode.map { it.toDomain() },
        hjelpestønadperioder = hjelpestønadperioder.map { it.toDomain() }
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketOmsorgsperiodeDb")
internal data class BeriketOmsorgsperiodeDb(
    val fom: String,
    val tom: String,
    val omsorgstype: String,
    val omsorgsmottaker: PersonDb,
    val kilde: KildeDb,
    val utbetalt: Int,
    val landstilknytning: LandstilknytningDb
)

internal fun Omsorgsperiode.toDb(): BeriketOmsorgsperiodeDb {
    return BeriketOmsorgsperiodeDb(
        fom = fom.toString(),
        tom = tom.toString(),
        omsorgstype = omsorgstype.toString(),
        omsorgsmottaker = omsorgsmottaker.toDb(),
        kilde = kilde.toDb(),
        utbetalt = utbetalt,
        landstilknytning = landstilknytning.toDb(),
    )
}

internal fun BeriketOmsorgsperiodeDb.toDomain(): Omsorgsperiode {
    return Omsorgsperiode(
        fom = YearMonth.parse(fom),
        tom = YearMonth.parse(tom),
        omsorgstype = DomainOmsorgstype.valueOf(omsorgstype),
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        kilde = kilde.toDomain(),
        utbetalt = utbetalt,
        landstilknytning = landstilknytning.toDomain(),
    )
}

@JsonTypeName("BeriketOmsorgsperiodeHjelpestønad")
internal data class BeriketOmsorgsperiodeHjelpestønad(
    val fom: String,
    val tom: String,
    val omsorgstype: String,
    val omsorgsmottaker: PersonDb,
    val kilde: KildeDb,
)

internal fun Hjelpestønadperiode.toDb(): BeriketOmsorgsperiodeHjelpestønad {
    return BeriketOmsorgsperiodeHjelpestønad(
        fom = fom.toString(),
        tom = tom.toString(),
        omsorgstype = omsorgstype.toString(),
        omsorgsmottaker = omsorgsmottaker.toDb(),
        kilde = kilde.toDb(),
    )
}

internal fun BeriketOmsorgsperiodeHjelpestønad.toDomain(): Hjelpestønadperiode {
    return Hjelpestønadperiode(
        fom = YearMonth.parse(fom),
        tom = YearMonth.parse(tom),
        omsorgstype = DomainOmsorgstype.valueOf(omsorgstype),
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        kilde = kilde.toDomain(),
    )
}
