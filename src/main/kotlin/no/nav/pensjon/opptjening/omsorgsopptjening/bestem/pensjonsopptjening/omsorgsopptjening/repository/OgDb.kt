package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OgVurdering
import java.util.LinkedList

@JsonTypeName("Og")
internal data class OgDb(
    val og: List<VilkårsvurderingDb>,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OgVurdering.toDb(): OgDb {
    return OgDb(
        og = mapRecursive(LinkedList(grunnlag), emptyList()),
        utfall = utfall.toDb()
    )
}

internal fun OgDb.toDomain(): OgVurdering {
    return OgVurdering(
        grunnlag = og.toDomain(),
        utfall = utfall.toDomain()
    )
}