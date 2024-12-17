package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash

import io.getunleash.Unleash
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant

class UnleashWrapper(
    private val unleash: Unleash,
    private val clock: Clock,
) {
    private val cache: MutableMap<NavUnleashConfig.Feature, CacheValue> = mutableMapOf()
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun isEnabled(toggle: NavUnleashConfig.Feature): Boolean {
        return cache[toggle]?.let {
            if (Duration.between(it.timestamp, getNow()).toSeconds() > 30) {
                refreshAndGet(toggle)
            } else {
                it.enabled
            }
        } ?: refreshAndGet(toggle)
    }

    private fun refreshAndGet(toggle: NavUnleashConfig.Feature): Boolean {
        log.info("Refreshing toggle: ${toggle.toggleName}, existing value: ${cache[toggle]}")
        return unleash.isEnabled(toggle.toggleName)
            .let {
                cache[toggle] = CacheValue(it, getNow())
                log.info("Toggle refreshed: ${toggle.toggleName}, new value: ${cache[toggle]}")
            }
            .let { cache[toggle]!!.enabled }
    }

    private fun getNow(): Instant {
        return Instant.now(clock)
    }

    private data class CacheValue(
        val enabled: Boolean,
        val timestamp: Instant
    )
}