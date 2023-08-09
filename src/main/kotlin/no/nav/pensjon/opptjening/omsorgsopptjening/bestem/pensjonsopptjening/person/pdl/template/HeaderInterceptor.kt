package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.template

import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.util.*

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
            add("Nav-Call-Id", UUID.randomUUID().toString())
            add("Nav-Consumer-Id", "omsorgsopptjening-bestem-pensjonsopptjening")
            add("Tema", "PEN")
            accept = listOf(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(tokenProvider.getToken())
        }

        return execution.execute(request, body)
    }
}