package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

interface BrevClient {
    fun sendBrev(sakId: String, fnr: String, omsorgsår: Int): Journalpost
}

data class BrevClientException(val msg: String, val throwable: Throwable?): RuntimeException(msg, throwable)