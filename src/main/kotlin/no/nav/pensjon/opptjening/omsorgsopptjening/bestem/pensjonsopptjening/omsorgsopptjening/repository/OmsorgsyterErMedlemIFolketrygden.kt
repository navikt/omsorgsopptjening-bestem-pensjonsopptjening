package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErMedlemIFolketrygden

@JsonTypeName("OmsorgsyterErMedlemIFolketrygden")
internal data class OmsorgsyterErMedlemIFolketrygdenDb(
    val grunnlag: MedlemIFolketrygden,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterErMedlemIFolketrygden.Vurdering.toDb(): OmsorgsyterErMedlemIFolketrygdenDb {
    return OmsorgsyterErMedlemIFolketrygdenDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterErMedlemIFolketrygdenDb.toDomain(): OmsorgsyterErMedlemIFolketrygden.Vurdering {
    return OmsorgsyterErMedlemIFolketrygden.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
    )
}

@JsonTypeName("MedlemIFolketrygden")
internal data class MedlemIFolketrygden(
    val medlemskapsvurdering: LoveMeVurderingDb,
    val omsorgstype: OmsorgskategoriDb,
) : GrunnlagVilkårsvurderingDb()

internal fun MedlemIFolketrygden.toDomain(): OmsorgsyterErMedlemIFolketrygden.Grunnlag {
    return OmsorgsyterErMedlemIFolketrygden.Grunnlag(
        loveMEVurdering = medlemskapsvurdering.toDomain(),
        omsorgstype = omsorgstype.toDomain()
    )
}

internal fun OmsorgsyterErMedlemIFolketrygden.Grunnlag.toDb(): MedlemIFolketrygden {
    return MedlemIFolketrygden(
        medlemskapsvurdering = loveMEVurdering.toDb(),
        omsorgstype = omsorgstype.toDb()
    )
}