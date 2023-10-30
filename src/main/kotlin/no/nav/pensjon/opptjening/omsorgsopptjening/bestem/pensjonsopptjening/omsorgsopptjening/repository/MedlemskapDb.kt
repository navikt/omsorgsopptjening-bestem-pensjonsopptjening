package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Medlemskap

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
internal sealed class MedlemskapDb {

    abstract val kilde: KildeDb
    @JsonTypeName("Ja")
    data class Ja(
        override val kilde: KildeDb,
    ) : MedlemskapDb()
    @JsonTypeName("Nei")
    data class Nei(
        override val kilde: KildeDb,
    ) : MedlemskapDb()
    @JsonTypeName("Ukjent")
    data class Ukjent(
        override val kilde: KildeDb,
    ) : MedlemskapDb()
}

internal fun Medlemskap.toDb(): MedlemskapDb {
    return when(this){
        is Medlemskap.Ja -> MedlemskapDb.Ja(kilde.toDb())
        is Medlemskap.Nei -> MedlemskapDb.Nei(kilde.toDb())
        is Medlemskap.Ukjent -> MedlemskapDb.Ukjent(kilde.toDb())
    }
}

internal fun MedlemskapDb.toDomain(): Medlemskap {
    return when(this){
        is MedlemskapDb.Ja -> Medlemskap.Ja(kilde.toDomain())
        is MedlemskapDb.Nei -> Medlemskap.Nei(kilde.toDomain())
        is MedlemskapDb.Ukjent -> Medlemskap.Ukjent(kilde.toDomain())
    }
}