package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
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
    val medlemskapsvurdering: MedlemskapsgrunnlagDb,
    val omsorgsytersOmsorgsmåneder: OmsorgsmånederDb,
    val antallMånederRegel: AntallMånederRegelDb,
    val landstilknytningmåneder: Set<LandstilknytningMånedDb>
) : GrunnlagVilkårsvurderingDb()

internal fun MedlemIFolketrygden.toDomain(): OmsorgsyterErMedlemIFolketrygden.Grunnlag {
    return OmsorgsyterErMedlemIFolketrygden.Grunnlag(
        medlemskapsgrunnlag = medlemskapsvurdering.toDomain(),
        omsorgsmåneder = omsorgsytersOmsorgsmåneder.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
        landstilknytningMåneder = Landstilknytningmåneder(landstilknytningmåneder.toDomain())
    )
}

internal fun OmsorgsyterErMedlemIFolketrygden.Grunnlag.toDb(): MedlemIFolketrygden {
    return MedlemIFolketrygden(
        medlemskapsvurdering = medlemskapsgrunnlag.toDb(),
        omsorgsytersOmsorgsmåneder = omsorgsmåneder().toDb(),
        antallMånederRegel = antallMånederRegel.toDb(),
        landstilknytningmåneder = landstilknytningMåneder.toDb()
    )
}