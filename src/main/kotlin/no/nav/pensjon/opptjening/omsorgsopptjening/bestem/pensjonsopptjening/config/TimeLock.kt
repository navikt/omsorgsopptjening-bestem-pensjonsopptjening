package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import org.apache.commons.lang3.ObjectUtils.min
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

data class TimeLock(
    private val properties: Properties,
    private val clock: Clock
) {
    private var delayUntil: Instant = getNow().plus(properties.initialDelaySeconds)
    private var count = AtomicLong(0)

    fun isOpen(): Boolean {
        return delayUntil <= getNow()
    }

    fun lock() {
        val now = getNow()
        val toAdd = properties.maxDelaySeconds.dividedBy(10).multipliedBy(count.incrementAndGet()).toSeconds()
        val newUntil = now.plusSeconds(toAdd)
        val maxUntil = now.plusSeconds(properties.maxDelaySeconds.toSeconds())
        delayUntil = min(newUntil, maxUntil)
    }

    fun lockDuration(): Duration {
        return Duration.between(getNow(), delayUntil)
    }

    fun open() {
        delayUntil = getNow()
    }

    private fun getNow(): Instant {
        return Instant.now(clock)
    }

    data class Properties(
        val initialDelaySeconds: Duration = Duration.ofSeconds(60),
        val maxDelaySeconds: Duration = Duration.ofSeconds(120)
    )
}
