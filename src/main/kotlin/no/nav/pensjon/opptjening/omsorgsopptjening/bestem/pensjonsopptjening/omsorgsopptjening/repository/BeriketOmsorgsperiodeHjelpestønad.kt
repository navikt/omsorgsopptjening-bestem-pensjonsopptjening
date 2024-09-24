package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Hjelpestønadperiode
import java.time.YearMonth

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