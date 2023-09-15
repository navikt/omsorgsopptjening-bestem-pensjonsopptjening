package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

interface HentPensjonspoengClient {
    fun hentPensjonspoeng(fnr: String, år: Int, type: DomainOmsorgstype): Pensjonspoeng
}
data class Pensjonspoeng(
    val år: Int,
    val poeng: Double,
    val type: DomainOmsorgstype
)

data class HentPensjonspoengClientException(val msg: String) : RuntimeException(msg)

