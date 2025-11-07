package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import org.apache.commons.lang3.ObjectUtils.min
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

data class TimeLock(
    private val properties: Properties
) {
    private var delayUntil: Long = System.currentTimeMillis().plus(properties.initialDelaySeconds.toMillis())
    private var count = AtomicLong(0)

    fun isOpen(): Boolean {
        return (delayUntil - getNow()) <= 0
    }

    fun lock() {
        val now = System.currentTimeMillis()
        val toAdd = properties.maxDelaySeconds.dividedBy(10).multipliedBy(count.incrementAndGet()).toMillis()
        val newUntil = now.plus(toAdd)
        val maxUntil = now.plus(properties.maxDelaySeconds.toMillis())
        delayUntil = min(newUntil, maxUntil)
    }

    fun lockDuration(): Duration {
        val duration = max(0, delayUntil - getNow())
        return Duration.ofMillis(duration)
    }

    fun open() {
        delayUntil = getNow()
        count.set(0)
    }

    private fun getNow(): Long {
        return System.currentTimeMillis()
    }

    data class Properties(
        val initialDelaySeconds: Duration = Duration.ofSeconds(60),
        val maxDelaySeconds: Duration = Duration.ofSeconds(120)
    )
}
