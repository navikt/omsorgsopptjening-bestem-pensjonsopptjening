package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterOppfyllerAlderskrav

@JsonTypeName("OmsorgsyterOppfyllerAlderskrav")
internal data class OmsorgsyterOppfyllerAlderskravDb(
    val grunnlag: AldersvurderingGrunnlag,
    val gyldigAldersintervall: Aldersintervall,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterOppfyllerAlderskrav.Vurdering.toDb(): OmsorgsyterOppfyllerAlderskravDb {
    return OmsorgsyterOppfyllerAlderskravDb(
        grunnlag = grunnlag.toDb(),
        gyldigAldersintervall = gyldigAldersintervall.toDb(),
        utfall = utfall.toDb(),
    )
}

internal fun OmsorgsyterOppfyllerAlderskravDb.toDomain(): OmsorgsyterOppfyllerAlderskrav.Vurdering {
    return OmsorgsyterOppfyllerAlderskrav.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}