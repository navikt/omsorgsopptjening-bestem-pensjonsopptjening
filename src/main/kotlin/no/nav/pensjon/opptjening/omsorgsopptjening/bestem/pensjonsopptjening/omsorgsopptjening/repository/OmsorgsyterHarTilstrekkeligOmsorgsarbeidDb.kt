package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid

@JsonTypeName("OmsorgsyterHarTilstrekkeligOmsorgsarbeid")
internal data class OmsorgsyterHarTilstrekkeligOmsorgsarbeidDb(
    val grunnlag: TilstrekkeligOmsorgsarbeid,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering.toDb(): OmsorgsyterHarTilstrekkeligOmsorgsarbeidDb {
    return OmsorgsyterHarTilstrekkeligOmsorgsarbeidDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterHarTilstrekkeligOmsorgsarbeidDb.toDomain(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering {
    return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}

@JsonTypeName("TilstrekkeligOmsorgsarbeid")
internal data class TilstrekkeligOmsorgsarbeid(
    val omsorgsytersOmsorgsmånederForOmsorgsmottaker: OmsorgsmånederDb,
    val antallMånederRegel: AntallMånederRegelDb
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.toDb(): TilstrekkeligOmsorgsarbeid {
    return TilstrekkeligOmsorgsarbeid(
        omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsmåneder().toDb(),
        antallMånederRegel = antallMånederRegel.toDb()
    )
}

internal fun TilstrekkeligOmsorgsarbeid.toDomain(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag {
    return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.persistent(
        omsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
    )
}
