package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import java.time.Year

interface BrevClient {
    fun sendBrev(sakId: String, eksternReferanseId: EksternReferanseId, omsorgsår: Year, språk: BrevSpraak? = null): Journalpost
}

data class BrevClientException(val msg: String, val throwable: Throwable? = null): RuntimeException(msg, throwable)

data class EksternReferanseId(val value:String)

enum class BrevSpraak {
    EN, NB, NN
}
