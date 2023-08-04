package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mapAnyToJson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Swagger: https://oppgave.dev.intern.nav.no/
 */
@Component
class OppgaveKlient(
    @Value("\${OPPGAVE_URL}") private val oppgaveUrl: String,
    val restTemplate: RestTemplate,
    private val registry: MeterRegistry
)
{
    private val antallOpprettedeOppgaver = registry.counter("oppgaver", "antall", "opprettet")
    private val logger = LoggerFactory.getLogger(OppgaveKlient::class.java)

    fun opprettOppgave(aktoerId: String, sakId: String, beskrivelse: String, tildeltEnhetsnr: String): String {
        val oppgaveRequest = OppgaveRequest(
            oppgavetype = OppgaveRequest.OppgaveType.KRAV.toString(),
            tema = OppgaveRequest.Tema.PENSJON.toString(),
            behandlingstema = OppgaveRequest.Behandlingstema.OMSORG.toString(),
            temagruppe =  OppgaveRequest.Temagruppe.PENSJON.toString(),
            prioritet = OppgaveRequest.Prioritet.NORM.toString(),
            aktoerId = aktoerId,
            aktivDato = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            opprettetAvEnhetsnr = "9999",
            tildeltEnhetsnr = tildeltEnhetsnr,
            saksreferanse = sakId,
            fristFerdigstillelse = LocalDate.now().plusDays(1).toString(),

            beskrivelse = beskrivelse
        )
        val requestBody = mapAnyToJson(oppgaveRequest, true)
        val httpEntity = HttpEntity(requestBody)
            return try {
                val response = restTemplate.exchange(oppgaveUrl, HttpMethod.POST, httpEntity, OppgaveResponse::class.java)
                logger.info("Opprettet kravoppgave for sakId: $sakId")
                antallOpprettedeOppgaver.increment()
                response.body!!.id
            } catch(ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under opprettelse av oppgave ex: $ex body: ${ex.responseBodyAsString}")
                throw RuntimeException("En feil oppstod under opprettelse av oppgave ex: ${ex.message} body: ${ex.responseBodyAsString}", ex)
            } catch(ex: Exception) {
                logger.error("En feil oppstod under opprettelse av oppgave ex: $ex")
                throw RuntimeException("En feil oppstod under opprettelse av oppgave ex: ${ex.message}", ex)
            }
        }
}

private class OppgaveRequest(
    private val id: Long? = null,
    private val tildeltEnhetsnr: String? = null,
    private val endretAvEnhetsnr: String? = null,
    private val opprettetAvEnhetsnr: String? = null,
    private val journalpostId: String? = null,
    private val journalpostkilde: String? = null,
    private val behandlesAvApplikasjon: String? = null,
    private val saksreferanse: String? = null,
    private val bnr: String? = null,
    private val samhandlernr: String? = null,
    private val aktoerId: String? = null,
    private val orgnr: String? = null,
    private val tilordnetRessurs: String? = null,
    private val beskrivelse: String? = null,
    private val temagruppe: String? = null,
    private val tema: String? = null,
    private val behandlingstema: String? = null,
    private val oppgavetype: String? = null,
    private val behandlingstype: String? = null,
    private val prioritet: String? = null,
    private val versjon: String? = null,
    private val mappeId: String? = null,
    private val fristFerdigstillelse: String? = null,
    private val aktivDato: String? = null,
    private val opprettetTidspunkt: String? = null,
    private val opprettetAv: String? = null,
    private val endretAv: String? = null,
    private val ferdigstiltTidspunkt: String? = null,
    private val endretTidspunkt: String? = null,
    private val status: String? = null,
    private val metadata: Map<String, String>? = null
) {

    enum class OppgaveType : Code {
        KRAV {
            override fun toString() = "KRA"
            override fun decode() = "Krav"
        }
    }

    enum class Tema : Code {
        PENSJON {
            override fun toString() = "PEN"
            override fun decode() = "Pensjon"
        }
    }

    enum class Behandlingstema : Code {
        OMSORG {
            override fun toString() = "ab0149"
            override fun decode() = "Omsorgspenger"
        }
    }

    enum class Temagruppe : Code {
        PENSJON {
            override fun toString() = "PENS"
            override fun decode() = "Pensjon"
        }
    }

    enum class Prioritet {
        HOY,
        NORM,
        LAV
    }

    interface Code {
        fun decode(): String
    }

    enum class Beskrivelse {
        OPPG_OMSORGP_FLERE_MOTTAKERE {
            override fun toString() = "Godskr. omsorgspoeng, flere mottakere:¤Flere personer har mottatt barnetrygd samme \u00E5r for barnet under 6 \u00E5r med fnr {1}. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks m\u00E5neder, og hadde barnetrygd i desember m\u00E5ned. Bruker med fnr {0} mottok ogs\u00E5 barnetrygd for 6 m\u00E5neder i samme \u00E5r. Vurder hvem som skal ha omsorgspoengene."
        },
        OPPG_OMSORGP_FLERE_MOTTAKERE_FODSAR {
            override fun toString() = "Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme \u00E5r for barnet med fnr {0} i barnets f\u00F8dsels\u00E5r. Vurder hvem som skal ha omsorgspoengene."
        },
        OPPG_OMSORGP_MANUELL_BEHANDL {
            override fun toString() =  "Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."
        }
    }
}

data class OppgaveResponse(
     val id: String
)