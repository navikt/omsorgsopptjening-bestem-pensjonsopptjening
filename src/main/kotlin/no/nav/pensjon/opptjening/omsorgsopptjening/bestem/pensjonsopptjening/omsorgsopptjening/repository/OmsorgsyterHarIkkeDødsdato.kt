package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarIkkeDødsdato
import java.time.LocalDate

@JsonTypeName("OmsorgsyterHarIkkeDødsdatoDb")
internal data class OmsorgsyterHarIkkeDødsdatoDb(
    val grunnlag: OmsorgsyterDødsdatoDb,
    val utfall: VilkårsvurderingUtfallDb,
) : VilkårsvurderingDb()

internal fun OmsorgsyterHarIkkeDødsdato.Vurdering.toDb(): OmsorgsyterHarIkkeDødsdatoDb {
    return OmsorgsyterHarIkkeDødsdatoDb(
        grunnlag = grunnlag.toDb(),
        utfall = utfall.toDb()
    )
}

internal fun OmsorgsyterHarIkkeDødsdatoDb.toDomain(): OmsorgsyterHarIkkeDødsdato.Vurdering {
    return OmsorgsyterHarIkkeDødsdato.Vurdering(
        grunnlag = grunnlag.toDomain(),
        utfall = utfall.toDomain(),
    )
}

@JsonTypeName("OmsorgsyterDødsdatoDb")
internal data class OmsorgsyterDødsdatoDb(
    val dødsdato: LocalDate?,
) : GrunnlagVilkårsvurderingDb()

internal fun OmsorgsyterDødsdatoDb.toDomain(): OmsorgsyterHarIkkeDødsdato.Grunnlag {
    return OmsorgsyterHarIkkeDødsdato.Grunnlag(
        dødsdato = dødsdato
    )
}

internal fun OmsorgsyterHarIkkeDødsdato.Grunnlag.toDb(): OmsorgsyterDødsdatoDb {
    return OmsorgsyterDødsdatoDb(
        dødsdato = dødsdato
    )
}