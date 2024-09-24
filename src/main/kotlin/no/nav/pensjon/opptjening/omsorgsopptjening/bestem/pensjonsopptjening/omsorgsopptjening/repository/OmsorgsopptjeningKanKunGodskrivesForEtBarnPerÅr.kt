package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import java.util.UUID

@JsonTypeName("OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr")
internal data class OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrDb(
    val grunnlag: KanKunGodskrivesEtBarnPerÅr,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()


internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering.toDb(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrDb {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅrDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}


@JsonTypeName("KanKunGodskrivesEtBarnPerÅr")
internal data class KanKunGodskrivesEtBarnPerÅr(
    val omsorgsmottaker: String,
    val omsorgsAr: Int,
    val behandlinger: List<FullførteBehandlingForOmsorgsyterDb>
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.toDb(): KanKunGodskrivesEtBarnPerÅr {
    return KanKunGodskrivesEtBarnPerÅr(
        omsorgsmottaker = omsorgsmottaker,
        omsorgsAr = omsorgsår,
        behandlinger = behandlinger.toDb()
    )
}

internal fun KanKunGodskrivesEtBarnPerÅr.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
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
@JsonTypeName("FullførteBehandlingForOmsorgsyterDb")
internal data class FullførteBehandlingForOmsorgsyterDb(
    val behandlingsId: UUID,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val år: Int,
    val erInnvilget: Boolean,
)

internal fun List<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter>.toDb(): List<FullførteBehandlingForOmsorgsyterDb> {
    return map { it.toDb() }
}

internal fun List<FullførteBehandlingForOmsorgsyterDb>.toDomain(): List<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter> {
    return map { it.toDomain() }
}

internal fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter.toDb(): FullførteBehandlingForOmsorgsyterDb {
    return FullførteBehandlingForOmsorgsyterDb(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        år = omsorgsÅr,
        erInnvilget = erInnvilget,
    )
}

internal fun FullførteBehandlingForOmsorgsyterDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter {
    return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsÅr = år,
        erInnvilget = erInnvilget
    )
}