package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class TestKlokke(
    private val initialClock: Clock = fixed(Instant.now(), ZoneOffset.UTC),
) : Clock() {
    private var nextInstant = initialClock.instant()
    private var overstyrtNeste: Instant? = null

    override fun getZone(): ZoneId = initialClock.zone

    override fun withZone(zone: ZoneId?): Clock = initialClock.withZone(zone)

    override fun instant(): Instant {
        return if (overstyrtNeste != null) {
            nextInstant = overstyrtNeste!!
            nextInstant
        } else {
            nextInstant = nextInstant.plus(1, ChronoUnit.SECONDS)
            nextInstant
        }

    }

    fun nåtid(): Instant {
        return nextInstant
    }

    fun nesteTikk(instant: Instant): TestKlokke {
        overstyrtNeste = instant
        return this
    }

    fun reset(){
        nextInstant = initialClock.instant()
        overstyrtNeste = null
    }
}