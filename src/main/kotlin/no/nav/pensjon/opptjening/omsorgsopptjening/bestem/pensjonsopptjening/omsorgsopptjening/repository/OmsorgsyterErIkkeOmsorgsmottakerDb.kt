package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErikkeOmsorgsmottaker

@JsonTypeName("OmsorgsyterErIkkeOmsorgsmottakerDb")
internal data class OmsorgsyterErIkkeOmsorgsmottakerDb(
    val grunnlag: OmsorgsyterOgOmsorgsmottakerDb,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterErikkeOmsorgsmottaker.Vurdering.toDb(): OmsorgsyterErIkkeOmsorgsmottakerDb {
    return OmsorgsyterErIkkeOmsorgsmottakerDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterErIkkeOmsorgsmottakerDb.toDomain(): OmsorgsyterErikkeOmsorgsmottaker.Vurdering {
    return OmsorgsyterErikkeOmsorgsmottaker.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
    )
}

@JsonTypeName("OmsorgsyterOgOmsorgsmottakerDb")
internal data class OmsorgsyterOgOmsorgsmottakerDb(
    val omsorgsyter: String,
    val omsorgsmottaker: String
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsyterOgOmsorgsmottakerDb.toDomain(): OmsorgsyterErikkeOmsorgsmottaker.Grunnlag {
    return OmsorgsyterErikkeOmsorgsmottaker.Grunnlag(
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker
    )
}

internal fun OmsorgsyterErikkeOmsorgsmottaker.Grunnlag.toDb(): OmsorgsyterOgOmsorgsmottakerDb {
    return OmsorgsyterOgOmsorgsmottakerDb(
        omsorgsyter = omsorgsyter,
        omsorgsmottaker = omsorgsmottaker
    )
}