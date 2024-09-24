package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarGyldigOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder

@JsonTypeName("OmsorgsyterHarGyldigOmsorgsarbeid")
internal data class OmsorgsyterHarGyldigOmsorgsarbeidDb(
    val grunnlag: GyldigOmsorgsarbeid,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering.toDb(): OmsorgsyterHarGyldigOmsorgsarbeidDb {
    return OmsorgsyterHarGyldigOmsorgsarbeidDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterHarGyldigOmsorgsarbeidDb.toDomain(): OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering {
    return OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
    )
}

@JsonTypeName("GyldigOmsorgsarbeid")
internal data class GyldigOmsorgsarbeid(
    val omsorgsytersUtbetalingsmåneder: Set<UtbetalingsmånedDb>,
    val omsorgsytersOmsorgsmåneder: OmsorgsmånederDb,
    val antallMånederRegel: AntallMånederRegelDb,
) : GrunnlagVilkårsvurderingDb()

internal fun GyldigOmsorgsarbeid.toDomain(): OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag {
    return OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag(
        omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(omsorgsytersUtbetalingsmåneder.map { it.toDomain() }
                                                                .toSet()),
        omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmåneder.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
    )
}

internal fun OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag.toDb(): GyldigOmsorgsarbeid {
    return GyldigOmsorgsarbeid(
        omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder.måneder.map { it.toDb() }.toSet(),
        omsorgsytersOmsorgsmåneder = omsorgsytersOmsorgsmåneder.toDb(),
        antallMånederRegel = antallMånederRegel.toDb(),
    )
}