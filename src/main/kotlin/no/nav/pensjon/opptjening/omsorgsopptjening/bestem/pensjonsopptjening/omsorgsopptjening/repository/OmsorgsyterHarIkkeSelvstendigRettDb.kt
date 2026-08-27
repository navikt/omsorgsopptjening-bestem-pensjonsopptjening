package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarIkkeSelvstendigRett

@JsonTypeName("OmsorgsyterHarIkkeSelvstendigRett")
internal data class OmsorgsyterHarIkkeSelvstendigRettDb(
    val grunnlag: SelvstendigRettDb,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterHarIkkeSelvstendigRett.Vurdering.toDb(): OmsorgsyterHarIkkeSelvstendigRettDb {
    return OmsorgsyterHarIkkeSelvstendigRettDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterHarIkkeSelvstendigRettDb.toDomain(): OmsorgsyterHarIkkeSelvstendigRett.Vurdering {
    return OmsorgsyterHarIkkeSelvstendigRett.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}

@JsonTypeName("SelvstendigRett")
internal data class SelvstendigRettDb(
    val selvstendigRettMåneder: SelvstendigRettMånederDb,
    val omsorgsytersOmsorgsmånederForOmsorgsmottaker: OmsorgsmånederDb,
    val antallMånederRegel: AntallMånederRegelDb
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsyterHarIkkeSelvstendigRett.Grunnlag.toDb(): SelvstendigRettDb {
    return SelvstendigRettDb(
        selvstendigRettMåneder = selvstendigRettMåneder.toDb(),
        omsorgsytersOmsorgsmånederForOmsorgsmottaker = omsorgsmåneder().toDb(),
        antallMånederRegel = antallMånederRegel.toDb()
    )
}

internal fun SelvstendigRettDb.toDomain(): OmsorgsyterHarIkkeSelvstendigRett.Grunnlag {
    return OmsorgsyterHarIkkeSelvstendigRett.Grunnlag.persistent(
        selvstendigRettMåneder = selvstendigRettMåneder.toDomain(),
        omsorgsmåneder = omsorgsytersOmsorgsmånederForOmsorgsmottaker.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
    )
}
