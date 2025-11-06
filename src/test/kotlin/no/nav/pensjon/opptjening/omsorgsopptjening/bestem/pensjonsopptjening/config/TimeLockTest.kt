package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.TestKlokke
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset

class TimeLockTest {

    private val fixedClock = Clock.fixed(
        LocalDateTime.of(2024, Month.OCTOBER, 1, 12, 0, 0).toInstant(ZoneOffset.UTC),
        ZoneOffset.UTC
    )

    @Test
    fun `åpen med en gang dersom ingen initial delay`() {
        val testKlokke = TestKlokke(fixedClock)
            .nesteTikk(fixedClock.instant())

        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            ),
            clock = testKlokke
        )

        assertThat(lock.isOpen()).isTrue()
    }

    @Test
    fun `stengt med en gang hvis initial delay`() {
        val testKlokke = TestKlokke(fixedClock)
            .nesteTikk(fixedClock.instant())

        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ofSeconds(3),
                maxDelaySeconds = Duration.ofSeconds(10),
            ),
            clock = testKlokke
        )

        assertThat(lock.isOpen()).isFalse()
        assertThat(lock.lockDuration()).isEqualTo(Duration.ofSeconds(3))
        testKlokke.nesteTikk(fixedClock.instant().plusSeconds(1))
        assertThat(lock.isOpen()).isFalse()
        testKlokke.nesteTikk(fixedClock.instant().plusSeconds(2))
        assertThat(lock.isOpen()).isFalse()
        testKlokke.nesteTikk(fixedClock.instant().plusSeconds(3))
        assertThat(lock.isOpen()).isTrue()
    }

    @Test
    fun `kan låse og åpne igjen`() {
        val testKlokke = TestKlokke(fixedClock)
            .nesteTikk(fixedClock.instant())

        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            ),
            clock = testKlokke
        )

        assertThat(lock.isOpen()).isTrue()
        lock.lock()
        assertThat(lock.isOpen()).isFalse()
        assertThat(lock.lockDuration()).isEqualTo(Duration.ofSeconds(1))
        testKlokke.nesteTikk(fixedClock.instant().plusSeconds(1))
        assertThat(lock.isOpen()).isTrue()
    }

    @Test
    fun `utvider låsetid inntil maks ved gjentakende kall til lås`() {
        val testKlokke = TestKlokke(fixedClock)
            .nesteTikk(fixedClock.instant())

        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            ),
            clock = testKlokke
        )

        assertThat(lock.isOpen()).isTrue()
        repeat(20) { lock.lock() }
        assertThat(lock.isOpen()).isFalse()

        testKlokke.nesteTikk(fixedClock.instant().plusSeconds(5))
        assertThat(lock.isOpen()).isFalse()

        testKlokke.nesteTikk(fixedClock.instant().plusSeconds(10))
        assertThat(lock.isOpen()).isTrue()
    }

    @Test
    fun `kan åpne låsen`() {
        val testKlokke = TestKlokke(fixedClock)
            .nesteTikk(fixedClock.instant())

        val lock = TimeLock(
            TimeLock.Properties(
                initialDelaySeconds = Duration.ZERO,
                maxDelaySeconds = Duration.ofSeconds(10),
            ),
            clock = testKlokke
        )

        repeat(10) { lock.lock() }
        assertThat(lock.isOpen()).isFalse()
        lock.open()
        assertThat(lock.isOpen()).isTrue()
    }
}