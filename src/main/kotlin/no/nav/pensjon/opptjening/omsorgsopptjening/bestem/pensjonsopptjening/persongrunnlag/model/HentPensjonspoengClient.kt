package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

interface HentPensjonspoengClient {
    fun hentPensjonspoengForOmsorgstype(fnr: String, år: Int, type: DomainOmsorgskategori): Pensjonspoeng.Omsorg
    fun hentPensjonspoengForInntekt(fnr: String, år: Int): Pensjonspoeng.Inntekt
}

data class HentPensjonspoengClientException(val msg: String) : RuntimeException(msg)

