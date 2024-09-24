package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId

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