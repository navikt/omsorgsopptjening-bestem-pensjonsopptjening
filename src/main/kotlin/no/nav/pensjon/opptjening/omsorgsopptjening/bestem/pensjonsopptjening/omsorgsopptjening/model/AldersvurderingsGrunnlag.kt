package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.LocalDate

data class AldersvurderingsGrunnlag(
    val person: AldersvurderingsPerson,
    val omsorgsAr: Int
) : ParagrafGrunnlag() {
    constructor(
        person: Person,
        omsorgsAr: Int,
    ) : this(
        person = AldersvurderingsPerson(
            fnr = person.fnr,
            fødselsdato = person.fødselsdato
        ),
        omsorgsAr = omsorgsAr,
    )

    fun erOppfylltFor(gyldigAldersintervall: IntRange): Boolean {
        return omsorgsAr - fødselsår() in gyldigAldersintervall
    }

    fun fødselsår(): Int {
        return person.fødselsdato.year
    }

    data class AldersvurderingsPerson(
        val fnr: String,
        val fødselsdato: LocalDate
    )
}