package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external

data class HentPensjonspoengListeRequest(
    val fnr : String,
    val fomAr: Int? = null,
    val tomAr: Int? = null,
    val pensjonspoengType : String? = null,
)