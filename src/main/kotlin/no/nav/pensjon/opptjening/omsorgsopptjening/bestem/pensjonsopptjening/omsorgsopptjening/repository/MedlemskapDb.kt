package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Medlemskap

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class MedlemskapDb {
    @JsonTypeName("Ja")
    class Ja() : MedlemskapDb()
    @JsonTypeName("Nei")
    class Nei : MedlemskapDb()
    @JsonTypeName("Ukjent")
    class Ukjent: MedlemskapDb()
}

internal fun Medlemskap.toDb(): MedlemskapDb {
    return when(this){
        Medlemskap.Ja -> MedlemskapDb.Ja()
        Medlemskap.Nei -> MedlemskapDb.Nei()
        Medlemskap.Ukjent -> MedlemskapDb.Ukjent()
    }
}

internal fun MedlemskapDb.toDomain(): Medlemskap {
    return when(this){
        is MedlemskapDb.Ja -> Medlemskap.Ja
        is MedlemskapDb.Nei -> Medlemskap.Nei
        is MedlemskapDb.Ukjent -> Medlemskap.Ukjent
    }
}