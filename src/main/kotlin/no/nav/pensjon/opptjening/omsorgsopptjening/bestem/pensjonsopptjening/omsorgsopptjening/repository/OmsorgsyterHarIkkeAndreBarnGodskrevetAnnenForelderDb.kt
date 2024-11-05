package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn
import java.util.UUID

@JsonTypeName("OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDb")
internal data class OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDb(
    val grunnlag: OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbGrunnlag,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()


internal fun OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering.toDb(): OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDb {
    return OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDb.toDomain(): OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering {
    return OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}


@JsonTypeName("OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbGrunnlag")
internal data class OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbGrunnlag(
    val omsorgsmottaker: String,
    val omsorgsAr: Int,
    val behandlinger: List<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling>
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.toDb(): OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbGrunnlag {
    return OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbGrunnlag(
        omsorgsmottaker = omsorgsmottaker,
        omsorgsAr = omsorgsår,
        behandlinger = behandlinger.toDb()
    )
}

internal fun OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbGrunnlag.toDomain(): OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag {
    return OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag(
        omsorgsmottaker = omsorgsmottaker,
        omsorgsår = omsorgsAr,
        behandlinger = behandlinger.toDomain()
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling")
internal data class OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling(
    val behandlingsId: UUID,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val år: Int,
    val erForelderTilOmsorgsmottaker: Boolean,
    val utfall: BehandlingsutfallDb,
)

internal fun List<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker>.toDb(): List<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling> {
    return map { it.toDb() }
}

internal fun List<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling>.toDomain(): List<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker> {
    return map { it.toDomain() }
}

internal fun OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker.toDb(): OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling {
    return OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        år = omsorgsår,
        erForelderTilOmsorgsmottaker = erForelderTilOmsorgsmottaker,
        utfall = utfall.toDb(),
    )
}

internal fun OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarnDbFullførtbehandling.toDomain(): OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker {
    return OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag.FullførtBehandlingForAnnenOmsorgsmottaker(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsår = år,
        erForelderTilOmsorgsmottaker = erForelderTilOmsorgsmottaker,
        utfall = utfall.toDomain()
    )
}