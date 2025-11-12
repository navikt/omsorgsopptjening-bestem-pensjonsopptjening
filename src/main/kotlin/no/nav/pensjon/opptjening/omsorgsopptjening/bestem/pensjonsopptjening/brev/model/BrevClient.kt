package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import org.springframework.web.client.HttpClientErrorException
import java.time.Year

interface BrevClient {
    fun sendBrev(sakId: String, eksternReferanseId: EksternReferanseId, omsorgsår: Year, språk: BrevSpraak? = null): Journalpost
}

data class BrevClientException(val msg: String, val throwable: Throwable? = null): RuntimeException(msg, throwable){
    constructor(ex: HttpClientErrorException): this("Feil fra brevtjenesten, status:${ex.statusCode.value()}, body:${ex.responseBodyAsString}", ex)
}

/**
 * Ved journalføring er det duplikatkontroll på denne verdien, dvs at flere dokumenter med samme referanse blir
 * avvist med http 409 conflict.
 */
data class EksternReferanseId(val value:String)

enum class BrevSpraak {
    EN, NB, NN
}
