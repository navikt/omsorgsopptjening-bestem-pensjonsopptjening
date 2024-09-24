package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerOppfyllerAlderskravForBarnetrygd

@JsonTypeName("OmsorgsmottakerOppfyllerAlderskravForBarnetrygd")
internal data class OmsorgsmottakerOppfyllerAlderskravForBarnetrygdDb(
    val grunnlag: AldersvurderingGrunnlag,
    val gyldigAldersintervall: Aldersintervall,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering.toDb(): OmsorgsmottakerOppfyllerAlderskravForBarnetrygdDb {
    return OmsorgsmottakerOppfyllerAlderskravForBarnetrygdDb(
        grunnlag = grunnlag.toDb(),
        gyldigAldersintervall = gyldigAldersintervall.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsmottakerOppfyllerAlderskravForBarnetrygdDb.toDomain(): OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering {
    return OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain()
    )
}

