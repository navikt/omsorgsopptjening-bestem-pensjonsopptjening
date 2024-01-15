package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Protected
class BarnetrygdWebApi(
    private val persongrunnlagMeldingService: PersongrunnlagMeldingService,
    private val oppgaveService: OppgaveService,
    private val brevService: BrevService,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(BarnetrygdWebApi::class.java)
    }

    @PostMapping("/bestem/rekjor-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun rekjørMeldinger(@RequestParam("uuidliste") meldingerString: String,
                        @RequestParam("begrunnelse") begrunnelse: String? = null): ResponseEntity<String> {

        val meldinger = try {
            parseUUIDListe(meldingerString)
        } catch (ex: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        log.info("rekjør flere: begrunnelse: $begrunnelse")

        try {
            val responsStrenger =
                meldinger.map { id ->
                    try {
                        val nyId = persongrunnlagMeldingService.stoppOgOpprettKopiAvMelding(id, begrunnelse)
                        "$id OK, erstattet av: $nyId"
                    } catch (ex: Throwable) {
                        "$id: Feilet, ${ex::class.simpleName}"
                    }
                }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/stopp-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun stoppMeldinger(@RequestParam("uuidliste") meldingerString: String,
                       @RequestParam("begrunnelse") begrunnelse: String? = null): ResponseEntity<String> {
        return ResponseEntity.ok("Ikke implementert: stopp-flere")
    }

    @PostMapping("/bestem/avslutt-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun avsluttMeldinger(@RequestParam("uuidliste") meldingerString: String,
                         @RequestParam("begrunnelse") begrunnelse: String? = null): ResponseEntity<String> {
        return ResponseEntity.ok("Ikke implementert: avslutt-flere")
    }

    @PostMapping("/bestem/restart-oppgaver", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun restartOppgaver(@RequestParam("uuidliste") oppgaverString: String,
                        @RequestParam("begrunnelse") begrunnelse: String? = null): ResponseEntity<String> {

        val oppgaver = try {
            parseUUIDListe(oppgaverString)
        } catch (ex: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val responsStrenger =
                oppgaver.map { id ->
                    try {
                        val nyId = oppgaveService.restart(id)
                        val status = nyId?.let { "Restartet" } ?: { "Fant ikke oppgaven" }
                        "$id $status"
                    } catch (ex: Throwable) {
                        "$id: Feilet, ${ex::class.simpleName}"
                    }
                }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/kanseller-oppgaver", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun kansellerOppgaver(@RequestParam("uuidliste") oppgaverString: String,
                          @RequestParam("begrunnelse") begrunnelse: String? = null): ResponseEntity<String> {

        val oppgaver = try {
            parseUUIDListe(oppgaverString)
        } catch (ex: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val responsStrenger =
                oppgaver.map { id ->
                    try {
                        val nyId = oppgaveService.kanseller(id)
                        val status = nyId?.let { "Restartet" } ?: { "Fant ikke oppgaven" }
                        "$id $status"
                    } catch (ex: Throwable) {
                        "$id: Feilet, ${ex::class.simpleName}"
                    }
                }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/restart-brev", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun restartBrev(@RequestParam("uuidliste") oppgaverString: String,
                    @RequestParam("begrunnelse") begrunnelse: String? = null): ResponseEntity<String> {

        val brev = try {
            parseUUIDListe(oppgaverString)
        } catch (ex: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val responsStrenger =
                brev.map { id ->
                    try {
                        val retId = brevService.restart(id)
                        if (retId ==null) {
                            "$id: Fant ikke brevet"
                        } else {
                            "$id: Restartet"
                        }
                    } catch (ex: Throwable) {
                        "$id: Feilet, ${ex::class.simpleName}"
                    }
                }
            val respons = responsStrenger.joinToString("\n")
            return ResponseEntity.ok(respons)
        } catch (ex: Throwable) {
            return ResponseEntity.internalServerError()
                .contentType(TEXT_PLAIN)
                .body("Feil ved prosessering: $ex")
        }
    }

    private fun parseUUIDListe(meldingerString: String) : List<UUID> {
        return meldingerString.lines()
            .map { it.replace("[^0-9a-f]".toRegex(),"") }
            .filter { it.isNotEmpty() }
            .map { UUID.fromString(it.trim()) }
    }
}