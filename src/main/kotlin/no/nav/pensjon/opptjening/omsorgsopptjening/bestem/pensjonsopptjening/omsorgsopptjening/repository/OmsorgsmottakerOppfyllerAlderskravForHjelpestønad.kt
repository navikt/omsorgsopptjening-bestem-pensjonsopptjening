package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad

@JsonTypeName("OmsorgsmottakerOppfyllerAlderskravForHjelpestønad")
internal data class OmsorgsmottakerOppfyllerAlderskravForHjelpestønadDb(
    val grunnlag: AldersvurderingGrunnlag,
    val gyldigAldersintervall: Aldersintervall,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering.toDb(): OmsorgsmottakerOppfyllerAlderskravForHjelpestønadDb {
    return OmsorgsmottakerOppfyllerAlderskravForHjelpestønadDb(
        grunnlag = grunnlag.toDb(),
        gyldigAldersintervall = gyldigAldersintervall.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsmottakerOppfyllerAlderskravForHjelpestønadDb.toDomain(): OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering {
    return OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}
