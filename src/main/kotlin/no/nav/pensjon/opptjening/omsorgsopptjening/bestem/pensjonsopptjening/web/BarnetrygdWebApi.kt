package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Protected
class BarnetrygdWebApi(
    private val persongrunnlagMeldingService: PersongrunnlagMeldingService,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(BarnetrygdWebApi::class.java)
    }

    @GetMapping("/bestem/rekjor/{meldingId}")
    fun rekjørMelding(@PathVariable meldingId: UUID): ResponseEntity<String> {
        persongrunnlagMeldingService.stoppMelding(meldingId)
        // TODO: rekjør
        return ResponseEntity.ok("Melding avsluttet")
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
    fun avslettAlleFeilede(): ResponseEntity<String> {
        log.info("""Avsluttet behandling av alle feilede meldinger for innlesning""")
        // TODO: Implementere denne
        throw NotImplementedError("Avslutting acv alle meldinger er ikke implementert")
    }

}