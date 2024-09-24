package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import java.util.UUID

@JsonTypeName("OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter")
internal data class OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterDb(
    val grunnlag: KanKunGodskrivesEnOmsorgsyter,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering.toDb(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterDb {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}

@JsonTypeName("KanKunGodskrivesEnOmsorgsyter")
internal data class KanKunGodskrivesEnOmsorgsyter(
    val omsorgsAr: Int,
    val behandlinger: List<FullførteBehandlingerForOmsorgsmottakerDb>
) : GrunnlagVilkårsvurderingDb()


internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.toDb(): KanKunGodskrivesEnOmsorgsyter {
    return KanKunGodskrivesEnOmsorgsyter(
        omsorgsAr = omsorgsår,
        behandlinger = fullførteBehandlinger.toDbs()
    )
}

internal fun KanKunGodskrivesEnOmsorgsyter.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag(
        omsorgsår = omsorgsAr,
        fullførteBehandlinger = behandlinger.toDomain()
    )
}


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("FullførteBehandlingerForOmsorgsmottakerDb")
data class FullførteBehandlingerForOmsorgsmottakerDb(
    val behandlingsId: UUID,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgsAr: Int,
    val erInnvilget: Boolean
)

internal fun List<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker>.toDbs(): List<FullførteBehandlingerForOmsorgsmottakerDb> {
    return map { it.toDb() }
}

internal fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker.toDb(): FullførteBehandlingerForOmsorgsmottakerDb {
    return FullførteBehandlingerForOmsorgsmottakerDb(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsAr = omsorgsår,
        erInnvilget = erInnvilget,
    )
}

@JvmName("FullførteBehandlingerForOmsorgsmottakerOgÅrDb")
internal fun List<FullførteBehandlingerForOmsorgsmottakerDb>.toDomain(): List<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker> {
    return map { it.toDomain() }
}

internal fun FullførteBehandlingerForOmsorgsmottakerDb.toDomain(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker {
    return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
        behandlingsId = behandlingsId,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgsår = omsorgsAr,
        erInnvilget = erInnvilget
    )
}