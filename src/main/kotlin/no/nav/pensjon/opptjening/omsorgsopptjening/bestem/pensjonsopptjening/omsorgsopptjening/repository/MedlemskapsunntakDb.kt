package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("MedlemskapsgrunnlagDb")
internal data class MedlemskapsgrunnlagDb(
    val medlemskapsunntak: MedlemskapsunntakDb,
)

internal fun Medlemskapsgrunnlag.toDb(): MedlemskapsgrunnlagDb {
    return MedlemskapsgrunnlagDb(
        medlemskapsunntak = medlemskapsunntak.toDb()
    )
}

internal fun MedlemskapsgrunnlagDb.toDomain(): Medlemskapsgrunnlag {
    return Medlemskapsgrunnlag(
        medlemskapsunntak = medlemskapsunntak.toDomain()
    )
}
