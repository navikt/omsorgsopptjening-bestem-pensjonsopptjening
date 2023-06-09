package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling

internal class BehandlingDb(
    val id: Long? = null,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgstype: OmsorgstypeDb,
    val grunnlag: BeriketGrunnlagDb,
    val vilkårsvurdering: VilkårsvurderingDb,
    val utfall: BehandlingsutfallDb
)

internal fun Behandling.toDb(): BehandlingDb {
    return BehandlingDb(
        omsorgsAr = omsorgsår(),
        omsorgsyter = omsorgsyter().fnr,
        omsorgstype = omsorgstype().toDb(),
        grunnlag = grunnlag().toDb(),
        vilkårsvurdering = vilkårsvurdering().toDb(),
        utfall = utfall().toDb()
    )
}

internal fun BehandlingDb.toDomain(): FullførtBehandling {
    val omsorgsyter = this.grunnlag.grunnlag.omsorgsyter

    return FullførtBehandling(
        omsorgsAr = omsorgsAr,
        omsorgsyter = omsorgsyter.toDomain(),
        omsorgstype = omsorgstype.toDomain(),
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
        vilkårsvurdering = vilkårsvurdering.toDomain()
    )
}
internal fun List<BehandlingDb>.toDomain(): List<FullførtBehandling> {
    return map { it.toDomain() }
}


