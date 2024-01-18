package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external

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
import org.springframework.web.client.HttpClientErrorException
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
) {
    companion object {
        private val log = LoggerFactory.getLogger(OppgaveKlient::class.java)
        private val secureLog = LoggerFactory.getLogger("secure")
    }

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
            val response =
                restTemplate.exchange(oppgaveUrl, HttpMethod.POST, httpEntity, OpprettOppgaveResponse::class.java)
            response.body!!.id.toString()
        } catch (ex: Exception) {
            """Feil ved kall til $oppgaveUrl, feil: ${ex::class.qualifiedName}""".let {
                log.warn(it)
                secureLog.warn(it, ex)
                throw OppgaveKlientException(it, ex)
            }
        }
    }

    fun hentOppgaveInfo(
        oppgaveId: String,
    ): OppgaveInfo {
        val requestEntity = HttpEntity<Any>(
            HttpHeaders().apply {
                add(CorrelationId.identifier, Mdc.getCorrelationId())
                add(InnlesingId.identifier, Mdc.getInnlesingId())
                add("X-Correlation-ID", Mdc.getCorrelationId()) //ulik casing fra CorrelationId.identifier
                accept = listOf(MediaType.APPLICATION_JSON)
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(tokenProvider.getToken())
            })
        val oppgaveUrl = "$oppgaveUrl/$oppgaveId"
        return try {
            val response =
                restTemplate.exchange(oppgaveUrl, HttpMethod.GET, requestEntity, HentOppgaveResponse::class.java)
                    .body!!
            OppgaveInfo(response.id, response.versjon, response.status)
        } catch (ex: Exception) {
            """Feil ved kall til $oppgaveUrl, feil: ${ex::class.qualifiedName}""".let {
                log.warn(it)
                secureLog.warn(it, ex)
                throw OppgaveKlientException(it, ex)
            }
        }
    }

    fun kansellerOppgave(
        oppgaveId: String,
        versjon: Int,
    ): KansellerOppgaveRespons {
        val kansellerRequest = KansellerOppgaveRequest(versjon)
        val requestBody = serialize(kansellerRequest)
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
        val oppgaveUrl = "$oppgaveUrl/$oppgaveId"
        return try {
            val response =
                restTemplate.exchange(oppgaveUrl, HttpMethod.PATCH, httpEntity, OpprettOppgaveResponse::class.java)
            log.info("kanseller oppgave: respons: ${response.statusCode}")
            if (response.statusCode.value() == 200) {
                KansellerOppgaveRespons.OPPDATERT_OK
            } else {
                throw RuntimeException("Kall lykkes med ukjent responskode: ${response.statusCode}")
            }
        } catch (ex: HttpClientErrorException.Conflict) {
            KansellerOppgaveRespons.OPPGAVE_OPPDATERT_I_PARALLELL
        } catch (ex: Exception) {
            """Feil ved kall til $oppgaveUrl, feil: ${ex::class.qualifiedName}""".let {
                log.warn(it)
                secureLog.warn(it, ex)
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

private data class KansellerOppgaveRequest(
    val versjon: Int,
) {
    val status = "FEILREGISTRERT"
}

private data class OpprettOppgaveResponse(
    val id: Long
)

private data class HentOppgaveResponse(
    val id: String,
    val status: OppgaveStatus,
    val versjon: Int,
)

class OppgaveKlientException(message: String, throwable: Throwable) : RuntimeException(message, throwable)

data class OppgaveInfo(val id: String, val versjon: Int, val status: OppgaveStatus)

enum class OppgaveStatus {
    OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT
}

enum class KansellerOppgaveRespons {
    OPPDATERT_OK,
    OPPGAVE_OPPDATERT_I_PARALLELL,
}