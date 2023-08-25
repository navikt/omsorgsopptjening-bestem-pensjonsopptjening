package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.template

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import pensjon.opptjening.azure.ad.client.TokenProvider

@Component
class HeaderInterceptor(
    private val tokenProvider: TokenProvider
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.apply {
            add("Nav-Call-Id", Mdc.getCorrelationId())
            add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
            add("Tema", "PEN")
            add(CorrelationId.identifier, Mdc.getCorrelationId())
            add(InnlesingId.identifier, Mdc.getInnlesingId())
            accept = listOf(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(tokenProvider.getToken())
        }

        return execution.execute(request, body)
    }
}