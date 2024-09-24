package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AldersvurderingsGrunnlag
import java.time.LocalDate

@JsonTypeName("AldersvurderingGrunnlag")
internal data class AldersvurderingGrunnlag(
    val fnr: String,
    val fødselsdato: LocalDate,
    val omsorgsAr: Int,
    val alder: Int,
) : GrunnlagVilkårsvurderingDb()


internal fun AldersvurderingGrunnlag.toDomain(): AldersvurderingsGrunnlag {
    return AldersvurderingsGrunnlag(
        person = AldersvurderingsGrunnlag.AldersvurderingsPerson(
            fnr = fnr,
            fødselsdato = fødselsdato,
        ),
        omsorgsAr = omsorgsAr,
        alder = alder,
    )
}

internal fun AldersvurderingsGrunnlag.toDb(): AldersvurderingGrunnlag {
    return AldersvurderingGrunnlag(
        fnr = person.fnr,
        fødselsdato = person.fødselsdato,
        omsorgsAr = omsorgsAr,
        alder = alder,
    )
}
