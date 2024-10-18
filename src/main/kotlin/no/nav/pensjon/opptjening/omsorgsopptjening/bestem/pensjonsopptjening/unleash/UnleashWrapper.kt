package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash

import io.getunleash.Unleash
import java.time.Clock
import java.time.Duration
import java.time.Instant

class UnleashWrapper(
    private val unleash: Unleash,
    private val clock: Clock,
) {
    private val cache: MutableMap<NavUnleashConfig.Feature, CacheValue> = mutableMapOf()

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
        return unleash.isEnabled(toggle.toggleName)
            .let { cache[toggle] = CacheValue(it, getNow()) }
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