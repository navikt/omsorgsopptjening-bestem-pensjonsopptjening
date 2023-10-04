package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import pensjon.opptjening.azure.ad.client.TokenProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Swagger: https://oppgave.dev.intern.nav.no/
 * https://kodeverk-web.dev.intern.nav.no/kodeverksoversikt/kodeverk
 * https://github.com/navikt/kodeverksmapper/blob/master/web/src/main/resources/underkategori.csv
 */
@Component
class OppgaveKlient(
    @Value("\${OPPGAVE_URL}") private val oppgaveUrl: String,
    @Qualifier("oppgaveTokenProvider") private val tokenProvider: TokenProvider,
    private val metrics: MicrometerMetrics,
) {
    private val logger = LoggerFactory.getLogger(OppgaveKlient::class.java)
    private val restTemplate = RestTemplateBuilder().build()

    fun opprettOppgave(
        aktoerId: String,
        sakId: String,
        beskrivelse: String,
        tildeltEnhetsnr: String
    ): String {
        val oppgaveRequest = OpprettOppgaveRequest(
            aktoerId = aktoerId,
            saksreferanse = sakId,
            beskrivelse = beskrivelse,
            tildeltEnhetsnr = tildeltEnhetsnr,
        )
        val requestBody = serialize(oppgaveRequest)
        val httpEntity = HttpEntity(
            requestBody,
            HttpHeaders().apply {
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                add("X-Correlation-ID", Mdc.getCorrelationId()) //ulik casing fra CorrelationId.identifier
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            }
        )
        return try {
            val response = restTemplate.exchange(oppgaveUrl, HttpMethod.POST, httpEntity, OppgaveResponse::class.java)
            logger.info("Opprettet kravoppgave for sakId: $sakId")
            metrics.antallOpprettedeOppgaver.increment()
            response.body!!.id.toString()
        } catch (ex: Exception) {
            """Feil ved kall til $oppgaveUrl, feil: $ex""".let {
                logger.warn(it, ex)
                throw OppgaveKlientException(it, ex)
            }
        }
    }
}

private data class OpprettOppgaveRequest(
    val aktoerId: String,
    val saksreferanse: String,
    val beskrivelse: String,
    val tildeltEnhetsnr: String,
) {
    val tema: String = Tema.PENSJON.toString()
    val behandlingstema: String = Behandlingstema.OMSORGSPOENG.toString()
    val oppgavetype: String = OppgaveType.KRAV.toString()
    val opprettetAvEnhetsnr: String = "9999"
    val aktivDato: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    val fristFerdigstillelse: String = LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_DATE)
    val prioritet: String = Prioritet.LAV.toString()

    enum class OppgaveType {
        KRAV {
            override fun toString() = "KRA"
        }
    }

    enum class Tema {
        PENSJON {
            override fun toString() = "PEN"
        }
    }

    enum class Behandlingstema {
        OMSORGSPOENG {
            override fun toString() = "ab0341"
        }
    }

    enum class Prioritet {
        HOY,
        NORM,
        LAV
    }
}

private data class OppgaveResponse(
    val id: Long
)

class OppgaveKlientException(message: String, throwable: Throwable) : RuntimeException(message, throwable)
