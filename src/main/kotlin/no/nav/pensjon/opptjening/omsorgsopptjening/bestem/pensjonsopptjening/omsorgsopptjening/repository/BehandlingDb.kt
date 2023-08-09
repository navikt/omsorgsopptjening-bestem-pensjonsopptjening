package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import java.time.Instant
import java.util.UUID

internal class BehandlingDb(
    val id: UUID? = null,
    val opprettet: Instant? = null,
    val omsorgsAr: Int,
    val omsorgsyter: String,
    val omsorgsmottaker: String,
    val omsorgstype: OmsorgstypeDb,
    val grunnlag: BarnetrygdGrunnlagDb,
    val vilkårsvurdering: VilkårsvurderingDb,
    val utfall: BehandlingsutfallDb,
    val kafkaMeldingId: UUID,
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
        kafkaMeldingId = kafkaMeldingId()
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
        kafkaMeldingId = kafkaMeldingId
    )
}

internal fun List<BehandlingDb>.toDomain(): List<FullførtBehandling> {
    return map { it.toDomain() }
}


