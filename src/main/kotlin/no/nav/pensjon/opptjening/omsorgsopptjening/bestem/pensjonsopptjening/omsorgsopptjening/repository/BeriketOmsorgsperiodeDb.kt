package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsperiode
import java.time.YearMonth

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BeriketOmsorgsperiodeDb")
internal data class BeriketOmsorgsperiodeDb(
    val fom: String,
    val tom: String,
    val omsorgstype: OmsorgstypeDb,
    val omsorgsmottaker: PersonDb,
    val kilde: KildeDb,
    val utbetalt: Int,
    val landstilknytning: LandstilknytningDb
)

internal fun Omsorgsperiode.toDb(): BeriketOmsorgsperiodeDb {
    return BeriketOmsorgsperiodeDb(
        fom = fom.toString(),
        tom = tom.toString(),
        omsorgstype = omsorgstype.toDb(),
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
        omsorgstype = omsorgstype.toDomain() as DomainOmsorgstype.Barnetrygd,
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        kilde = kilde.toDomain(),
        utbetalt = utbetalt,
        landstilknytning = landstilknytning.toDomain(),
    )
}