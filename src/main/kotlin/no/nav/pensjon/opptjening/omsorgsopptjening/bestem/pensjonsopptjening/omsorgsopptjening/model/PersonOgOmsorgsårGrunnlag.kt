package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model




data class PersonOgOmsorgsårGrunnlag(
    val person: Person,
    val omsorgsAr: Int
): ParagrafGrunnlag() {
    fun alderMottaker(mellom: IntRange): Boolean {
        return person.alderVedUtløpAv(omsorgsAr) in mellom
    }
}