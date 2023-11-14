package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external.PENBrevClient
import java.time.Year

interface BrevClient {
    fun sendBrev(sakId: String, fnr: String, omsorgsår: Year, språk: PENBrevClient.BrevSpraak? = null): Journalpost
}

data class BrevClientException(val msg: String, val throwable: Throwable?): RuntimeException(msg, throwable)