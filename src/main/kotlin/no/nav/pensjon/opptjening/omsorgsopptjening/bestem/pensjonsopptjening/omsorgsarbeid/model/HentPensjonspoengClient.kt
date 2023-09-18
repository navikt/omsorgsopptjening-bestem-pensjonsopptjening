package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

interface HentPensjonspoengClient {
    fun hentPensjonspoengForOmsorgsarbeid(fnr: String, år: Int, type: DomainOmsorgstype): Pensjonspoeng.Omsorg
    fun hentPensjonspoengForInntekt(fnr: String, år: Int): Pensjonspoeng.Inntekt
}

data class HentPensjonspoengClientException(val msg: String) : RuntimeException(msg)

