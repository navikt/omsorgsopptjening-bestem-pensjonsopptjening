package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad

@JsonTypeName("OmsorgsyterErForelderTilMottakerAvHjelpestønad")
internal data class OmsorgsyterErForelderTilMottakerAvHjelpestønadDb(
    val grunnlag: OmsorgsyterOgOmsorgsmottaker,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering.toDb(): OmsorgsyterErForelderTilMottakerAvHjelpestønadDb {
    return OmsorgsyterErForelderTilMottakerAvHjelpestønadDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterErForelderTilMottakerAvHjelpestønadDb.toDomain(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering {
    return OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}

@JsonTypeName("OmsorgsyterOgOmsorgsmottaker")
internal data class OmsorgsyterOgOmsorgsmottaker(
    val omsorgsyter: String,
    val omsorgsytersFamilierelasjoner: Map<String, String>,
    val omsorgsmottaker: String,
    val omsorgsmottakersFamilierelasjoner: Map<String, String>,
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsyterOgOmsorgsmottaker.toDomain(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag {
    return OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag(
        omsorgsyter = omsorgsyter,
        omsorgsytersFamilierelasjoner = omsorgsytersFamilierelasjoner.toDomain(),
        omsorgsmottaker = omsorgsmottaker,
        omsorgsmottakersFamilierelasjoner = omsorgsmottakersFamilierelasjoner.toDomain(),
    )
}

internal fun OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag.toDb(): OmsorgsyterOgOmsorgsmottaker {
    return OmsorgsyterOgOmsorgsmottaker(
        omsorgsyter = omsorgsyter,
        omsorgsytersFamilierelasjoner = omsorgsytersFamilierelasjoner.toDb(),
        omsorgsmottaker = omsorgsmottaker,
        omsorgsmottakersFamilierelasjoner = omsorgsmottakersFamilierelasjoner.toDb(),
    )
}