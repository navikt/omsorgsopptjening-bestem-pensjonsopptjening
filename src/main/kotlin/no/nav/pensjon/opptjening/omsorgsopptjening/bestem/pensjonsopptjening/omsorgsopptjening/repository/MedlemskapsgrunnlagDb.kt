package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("MedlemskapsgrunnlagDb")
internal data class MedlemskapsgrunnlagDb(
    val vurderingFraOppslag: LoveMeVurderingDb,
    val rådata: String,
)

internal fun Medlemskapsgrunnlag.toDb(): MedlemskapsgrunnlagDb {
    return MedlemskapsgrunnlagDb(
        vurderingFraOppslag = vurderingFraLoveME.toDb(),
        rådata = rådata,
    )
}

internal fun MedlemskapsgrunnlagDb.toDomain(): Medlemskapsgrunnlag {
    return Medlemskapsgrunnlag(
        vurderingFraLoveME = vurderingFraOppslag.toDomain(),
        rådata = rådata,
    )
}

internal fun Medlemskapsgrunnlag.LoveMeVurdering.toDb(): LoveMeVurderingDb {
    return when (this) {
        Medlemskapsgrunnlag.LoveMeVurdering.MEDLEM_I_FOLKETRYGDEN -> LoveMeVurderingDb.MEDLEM_I_FOLKETRYGDEN
        Medlemskapsgrunnlag.LoveMeVurdering.IKKE_MEDLEM_I_FOLKETRYGDEN -> LoveMeVurderingDb.IKKE_MEDLEM_I_FOLKETRYGDEN
        Medlemskapsgrunnlag.LoveMeVurdering.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN -> LoveMeVurderingDb.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN
    }
}

internal fun LoveMeVurderingDb.toDomain(): Medlemskapsgrunnlag.LoveMeVurdering {
    return when (this) {
        LoveMeVurderingDb.MEDLEM_I_FOLKETRYGDEN -> Medlemskapsgrunnlag.LoveMeVurdering.MEDLEM_I_FOLKETRYGDEN
        LoveMeVurderingDb.IKKE_MEDLEM_I_FOLKETRYGDEN -> Medlemskapsgrunnlag.LoveMeVurdering.IKKE_MEDLEM_I_FOLKETRYGDEN
        LoveMeVurderingDb.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN -> Medlemskapsgrunnlag.LoveMeVurdering.UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN
    }
}

internal enum class LoveMeVurderingDb {
    MEDLEM_I_FOLKETRYGDEN,
    IKKE_MEDLEM_I_FOLKETRYGDEN,
    UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN,
}
