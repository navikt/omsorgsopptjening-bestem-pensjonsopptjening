package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Landstilknytningmåneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelsemåneder
import java.time.YearMonth

@JsonTypeName("OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDb")
internal data class OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDb(
    val grunnlag: OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDbGrunnlag,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering.toDb(): OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDb {
    return OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDb.toDomain(): OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering {
    return OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
    )
}

@JsonTypeName("OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsGrunnlagDb")
internal data class OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDbGrunnlag(
    val omsorgsmåneder: OmsorgsmånederDb,
    val ytelsemåneder: Set<YearMonth>,
    val landstilknytningmåneder: Set<LandstilknytningMånedDb>,
    val antallMånederRegel: AntallMånederRegelDb,
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDbGrunnlag.toDomain(): OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag {
    return OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag(
        omsorgsmåneder = omsorgsmåneder.toDomain(),
        ytelsemåneder = Ytelsemåneder(ytelsemåneder),
        landstilknytningmåneder = Landstilknytningmåneder(landstilknytningmåneder.toDomain()),
        antallMånederRegel = antallMånederRegel.toDomain()

    )
}

internal fun OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag.toDb(): OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDbGrunnlag {
    return OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøsDbGrunnlag(
        omsorgsmåneder = omsorgsmåneder().toDb(),
        ytelsemåneder = ytelsemåneder.alle(),
        landstilknytningmåneder = landstilknytningmåneder.toDb(),
        antallMånederRegel = antallMånederRegel.toDb(),
    )
}