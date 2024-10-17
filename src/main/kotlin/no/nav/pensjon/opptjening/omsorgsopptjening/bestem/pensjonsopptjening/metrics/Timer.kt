package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("timer")

inline fun <reified T> timeAndLogElapsed(identifier: String, block: () -> T): T {
    val start = System.currentTimeMillis()
    return block().also { log.info("$identifier took: ${System.currentTimeMillis() - start}") }
}