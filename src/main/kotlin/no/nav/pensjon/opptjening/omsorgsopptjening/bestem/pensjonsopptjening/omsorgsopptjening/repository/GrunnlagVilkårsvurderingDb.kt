package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterOgOmsorgsårGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullOmsorgForBarnUnder6Grunnlag

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = GrunnlagVilkårsvurderingDb.OmsorgForBarnUnder6::class,
        name = "OmsorgForBarnUnder6",
    ),
    JsonSubTypes.Type(
        value = GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr::class,
        name = "OmsorgsyterOgOmsorgsÅr",
    ),
)
sealed class GrunnlagVilkårsvurderingDb {
    internal data class OmsorgForBarnUnder6(
        val omsorgsAr: Int,
        val omsorgsmottaker: PersonMedFødselsårDb,
        val antallMånederFullOmsorg: Int,
    ) : GrunnlagVilkårsvurderingDb()

    internal data class OmsorgsyterOgOmsorgsÅr(
        val omsorgsyter: PersonMedFødselsårDb,
        val omsorgsAr: Int
    ) : GrunnlagVilkårsvurderingDb()
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgForBarnUnder6.toDomain(): FullOmsorgForBarnUnder6Grunnlag {
    return FullOmsorgForBarnUnder6Grunnlag(
        omsorgsAr = omsorgsAr,
        omsorgsmottaker = omsorgsmottaker.toDomain(),
        antallMånederFullOmsorg = antallMånederFullOmsorg
    )
}

internal fun GrunnlagVilkårsvurderingDb.OmsorgsyterOgOmsorgsÅr.toDomain(): OmsorgsyterOgOmsorgsårGrunnlag {
    return OmsorgsyterOgOmsorgsårGrunnlag(
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgsAr = omsorgsAr
    )
}


