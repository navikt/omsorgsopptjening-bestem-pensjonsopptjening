package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErMedlemIFolketrygden
import java.time.YearMonth

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
    val ikkeMedlem: Set<YearMonth>,
    val pliktigEllerFrivillig: Set<YearMonth>,
    val omsorgsytersOmsorgsmåneder: OmsorgsmånederDb,
    val antallMånederRegel: AntallMånederRegelDb,
    val landstilknytningmåneder: Set<LandstilknytningMånedDb>
) : GrunnlagVilkårsvurderingDb()

internal fun MedlemIFolketrygden.toDomain(): OmsorgsyterErMedlemIFolketrygden.Grunnlag {
    return OmsorgsyterErMedlemIFolketrygden.Grunnlag.persistent(
        ikkeMedlem = ikkeMedlem,
        pliktigEllerFrivillig = pliktigEllerFrivillig,
        omsorgsmåneder = omsorgsytersOmsorgsmåneder.toDomain(),
        antallMånederRegel = antallMånederRegel.toDomain(),
        landstilknytningMåneder = Landstilknytningmåneder(landstilknytningmåneder.toDomain())
    )
}

internal fun OmsorgsyterErMedlemIFolketrygden.Grunnlag.toDb(): MedlemIFolketrygden {
    return MedlemIFolketrygden(
        ikkeMedlem = ikkeMedlem,
        pliktigEllerFrivillig = pliktigEllerFrivillig,
        omsorgsytersOmsorgsmåneder = omsorgsmåneder().toDb(),
        antallMånederRegel = antallMånederRegel.toDb(),
        landstilknytningmåneder = landstilknytningMåneder.toDb()
    )
}