package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class TimeLockTest {

    @Test
    fun `åpen med en gang dersom ingen initial delay`() {
        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            )
        )

        assertThat(lock.isOpen()).isTrue()
    }

    @Test
    fun `stengt med en gang hvis initial delay`() {
        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ofSeconds(1),
                maxDelaySeconds = Duration.ofSeconds(10),
            )
        )

        assertThat(lock.isOpen()).isFalse()
        assertThat(lock.lockDuration()).isLessThanOrEqualTo(Duration.ofSeconds(1))
        Thread.sleep(1000)
        assertThat(lock.isOpen()).isTrue
    }

    @Test
    fun `kan låse og åpne igjen`() {
        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            )
        )

        assertThat(lock.isOpen()).isTrue()
        lock.lock()
        assertThat(lock.isOpen()).isFalse()
        assertThat(lock.lockDuration()).isLessThanOrEqualTo(Duration.ofSeconds(1))
        Thread.sleep(Duration.ofSeconds(1))
        assertThat(lock.isOpen()).isTrue()
    }

    @Test
    fun `utvider låsetid inntil maks ved gjentakende kall til lås`() {
        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            )
        )

        assertThat(lock.isOpen()).isTrue()
        repeat(20) { lock.lock() }
        assertThat(lock.isOpen()).isFalse()
        assertThat(lock.lockDuration()).isLessThanOrEqualTo(Duration.ofSeconds(10))
    }

    @Test
    fun `resetter låsetid og teller for backoff ved åpning`() {
        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            )
        )

        assertThat(lock.isOpen()).isTrue()
        repeat(20) { lock.lock() }
        assertThat(lock.isOpen()).isFalse()
        assertThat(lock.lockDuration()).isLessThanOrEqualTo(Duration.ofSeconds(10))
        lock.open()
        assertThat(lock.lockDuration()).isLessThanOrEqualTo(Duration.ofSeconds(0))
        lock.lock()
        assertThat(lock.lockDuration()).isLessThanOrEqualTo(Duration.ofSeconds(1))
    }

    @Test
    fun `kan åpne låsen`() {
        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            )
        )

        repeat(10) { lock.lock() }
        assertThat(lock.isOpen()).isFalse()
        lock.open()
        assertThat(lock.isOpen()).isTrue()
    }
}