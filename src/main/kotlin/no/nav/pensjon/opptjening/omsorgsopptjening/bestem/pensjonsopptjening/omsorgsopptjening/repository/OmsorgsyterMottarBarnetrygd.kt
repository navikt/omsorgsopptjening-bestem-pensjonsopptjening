package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Utbetalingsmåneder

@JsonTypeName("OmsorgsyterMottarBarnetrygd")
internal data class OmsorgsyterMottarBarnetrygdDb(
    val grunnlag: MottarBarnetrygd,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterMottarBarnetrgyd.Vurdering.toDb(): OmsorgsyterMottarBarnetrygdDb {
    return OmsorgsyterMottarBarnetrygdDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb(),
    )
}

internal fun OmsorgsyterMottarBarnetrygdDb.toDomain(): OmsorgsyterMottarBarnetrgyd.Vurdering {
    return OmsorgsyterMottarBarnetrgyd.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
    )
}

@JsonTypeName("MottarBarnetrygd")
internal data class MottarBarnetrygd(
    val omsorgsytersUtbetalingsmåneder: Set<UtbetalingsmånedDb>,
    val antallMånederRegel: AntallMånederRegelDb,
    val omsorgstype: OmsorgskategoriDb,
) : GrunnlagVilkårsvurderingDb()


internal fun MottarBarnetrygd.toDomain(): OmsorgsyterMottarBarnetrgyd.Grunnlag {
    return OmsorgsyterMottarBarnetrgyd.Grunnlag(
        omsorgsytersUtbetalingsmåneder = Utbetalingsmåneder(omsorgsytersUtbetalingsmåneder.map { it.toDomain() }
                                                                .toSet()),
        antallMånederRegel = antallMånederRegel.toDomain(),
        omsorgstype = omsorgstype.toDomain(),
    )
}

internal fun OmsorgsyterMottarBarnetrgyd.Grunnlag.toDb(): MottarBarnetrygd {
    return MottarBarnetrygd(
        omsorgsytersUtbetalingsmåneder = omsorgsytersUtbetalingsmåneder.måneder.map { it.toDb() }.toSet(),
        antallMånederRegel = antallMånederRegel.toDb(),
        omsorgstype = omsorgstype.toDb(),
    )
}
