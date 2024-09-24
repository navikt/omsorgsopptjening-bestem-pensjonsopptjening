package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.EllerVurdering
import java.util.LinkedList

@JsonTypeName("Eller")
internal data class EllerDb(
    val eller: List<VilkårsvurderingDb>,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()


internal fun EllerVurdering.toDb(): EllerDb {
    return EllerDb(
        eller = mapRecursive(LinkedList(grunnlag), emptyList()),
        utfall = utfall.toDb()
    )
}

internal fun EllerDb.toDomain(): EllerVurdering {
    return EllerVurdering(
        grunnlag = eller.toDomain(),
        utfall = utfall.toDomain()
    )
}