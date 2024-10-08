package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import java.time.Instant
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("BehandlingDb")
internal class BehandlingDb(
    val id: UUID? = null,
    val opprettet: Instant? = null,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgstype: OmsorgskategoriDb,
    val grunnlag: OmsorgsopptjeningGrunnlagDb,
    val vilkårsvurdering: VilkårsvurderingDb,
    val utfall: BehandlingsutfallDb,
    val meldingId: UUID,
)

internal fun Behandling.toDb(): BehandlingDb {
    return BehandlingDb(
        omsorgsAr = omsorgsår(),
        omsorgsyter = omsorgsyter().fnr,
        omsorgsmottaker = omsorgsmottaker().fnr,
        omsorgstype = omsorgstype().toDb(),
        grunnlag = grunnlag().toDb(),
        vilkårsvurdering = vilkårsvurdering().toDb(),
        utfall = utfall().toDb(),
        meldingId = meldingId()
    )
}

internal fun BehandlingDb.toDomain(): FullførtBehandling {
    return FullførtBehandling(
        id = id!!,
        opprettet = opprettet!!,
        omsorgsAr = omsorgsAr,
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker,
        omsorgstype = omsorgstype.toDomain(),
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
        vilkårsvurdering = vilkårsvurdering.toDomain(),
        meldingId = meldingId
    )
}

internal fun List<BehandlingDb>.toDomain(): List<FullførtBehandling> {
    return map { it.toDomain() }
}


