package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Protected
class BarnetrygdWebApi(
    private val persongrunnlagMeldingService: PersongrunnlagMeldingService,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(BarnetrygdWebApi::class.java)
    }

    @GetMapping("/bestem/rekjor/{meldingId}")
    fun rekjørMelding(@PathVariable meldingId: UUID): ResponseEntity<String> {
        persongrunnlagMeldingService.stoppOgOpprettKopiAvMelding(meldingId)
        return ResponseEntity.ok("Melding avsluttet: $meldingId")
    }

    @PostMapping("/bestem/rekjor-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun rekjørMeldinger(@RequestParam("uuidliste") meldingerString: String): ResponseEntity<String> {

        val meldinger = try {
            meldingerString.lines().map { UUID.fromString(it.trim()) }
        } catch (ex: Throwable) {
            return ResponseEntity.badRequest().body("Kunne ikke parse uuid'ene")
        }

        try {
            val responsStrenger =
                meldinger.map { id ->
                    try {
                        val nyId = persongrunnlagMeldingService.stoppOgOpprettKopiAvMelding(id)
                        "$id OK, erstattet av: $nyId"
                    } catch (ex: Throwable) {
                        "$id: Feilet, ${ex::class.simpleName}"
                    }
                }
            val respons = responsStrenger.joinToString("\n")
            println("meldinger: $meldinger")
            return ResponseEntity.ok(respons)
        } catch (ex) {
            return ResponseEntity.internalServerError().body("Feil ved prosessering: $ex")
        }
    }

    @PostMapping("/bestem/stopp-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun stoppMeldinger(@RequestParam("uuidliste") meldingerString: String): ResponseEntity<String> {
        return ResponseEntity.ok("Ikke implementert: stopp-flere")
    }

    @PostMapping("/bestem/avslutt-flere", consumes = [APPLICATION_FORM_URLENCODED_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun avsluttMeldinger(@RequestParam("uuidliste") meldingerString: String): ResponseEntity<String> {
        return ResponseEntity.ok("Ikke implementert: avslutt-flere")
    }

    @GetMapping("/bestem/rekjor/avslutt-alle/")
    fun rekjørAlleFeilede(@PathVariable meldingId: UUID): ResponseEntity<String> {
        log.info("""Avsluttet behandling av: $meldingId feilede rader for innlesning""")
        throw NotImplementedError()
    }

    @GetMapping("/bestem/avslutt/{meldingId}")
    fun avsluttMelding(@PathVariable meldingId: UUID): ResponseEntity<String> {
        log.info("""Avsluttet behandling av: $meldingId feilede rader for innlesning""")
        persongrunnlagMeldingService.avsluttMelding(meldingId)
        return ResponseEntity.ok("Melding avsluttet")
    }

    @GetMapping("/bestem/avslutt-alle/")
    fun avsluttAlleFeilede(): ResponseEntity<String> {
        log.info("""Avsluttet behandling av alle feilede meldinger for innlesning""")
        // TODO: Implementere denne
        throw NotImplementedError("Avslutting acv alle meldinger er ikke implementert")
    }
}