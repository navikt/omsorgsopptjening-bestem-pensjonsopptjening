package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

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
        persongrunnlagMeldingService.stoppMelding(meldingId)
        persongrunnlagMeldingService.opprettKopiAvStoppetMelding(meldingId)
        return ResponseEntity.ok("Melding avsluttet: $meldingId")
    }

    @PostMapping("/bestem/rekjor-flere", consumes = [TEXT_PLAIN_VALUE], produces = [TEXT_PLAIN_VALUE])
    fun rekjørMeldinger(@RequestParam("meldinger") meldingerString: String) {
        println("meldinger: $meldingerString")
        throw NotImplementedError("Ikke implementert")
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