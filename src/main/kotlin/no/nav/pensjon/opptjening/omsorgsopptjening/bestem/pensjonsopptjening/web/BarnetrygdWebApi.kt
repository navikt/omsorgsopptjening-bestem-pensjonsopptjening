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
        log.info("rekjør melding ${meldingId}")
        return ResponseEntity.ok("hello")
    }


    @GetMapping("/bestem/rekjor/avslutt-alle/")
    fun rekjørAlleFeilede(@PathVariable meldingId: UUID): ResponseEntity<String> {
        log.info("""Avsluttet behandling av: $meldingId feilede rader for innlesning""")
        return ResponseEntity.ok("hello")
    }

    @GetMapping("/bestem/avslutt/{meldingId}")
    fun avsluttFeiletMelding(@PathVariable meldingId: UUID): ResponseEntity<String> {
        log.info("""Avsluttet behandling av: $meldingId feilede rader for innlesning""")
        return ResponseEntity.ok("hello")
    }

    @GetMapping("/bestem/avslutt-alle/")
    fun avslettAlleFeilede(): ResponseEntity<String> {
        log.info("""Avsluttet behandling av alle feilede meldinger for innlesning""")
        return ResponseEntity.ok("hello")
    }

}