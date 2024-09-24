package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Persongrunnlag

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
    val medlemskapsgrunnlag: MedlemskapsgrunnlagDb
)

internal fun Persongrunnlag.toDb(): BeriketPersongrunnlagDb {
    return BeriketPersongrunnlagDb(
        omsorgsyter = omsorgsyter.toDb(),
        omsorgVedtakPeriode = omsorgsperioder.map { it.toDb() },
        hjelpestønadperioder = hjelpestønadperioder.map { it.toDb() },
        medlemskapsgrunnlag = medlemskapsgrunnlag.toDb()
    )
}

internal fun BeriketPersongrunnlagDb.toDomain(): Persongrunnlag {
    return Persongrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsperioder = omsorgVedtakPeriode.map { it.toDomain() },
        hjelpestønadperioder = hjelpestønadperioder.map { it.toDomain() },
        medlemskapsgrunnlag = medlemskapsgrunnlag.toDomain(),
    )
}