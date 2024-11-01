package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.TestKlokke
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset

class UnleashWrapperTest {

    private val unleash: Unleash = mock()
    private val clock: TestKlokke = TestKlokke(
        Clock.fixed(
            LocalDateTime.of(2024, Month.OCTOBER, 1, 12, 0, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
        )
    )


    @Test
    fun `henter fra unleash hvis ingen cached`() {
        whenever(unleash.isEnabled(any())).thenReturn(true)

        val enabled = UnleashWrapper(
            unleash = unleash,
            clock = clock
        ).isEnabled(NavUnleashConfig.Feature.GODSKRIV)

        assertThat(enabled).isTrue()
        verify(unleash).isEnabled(NavUnleashConfig.Feature.GODSKRIV.toggleName)
    }

    @Test
    fun `henter fra cache hvis verdi eksisterer og er gyldig`() {
        whenever(unleash.isEnabled(any())).thenReturn(true)

        val wrapper = UnleashWrapper(
            unleash = unleash,
            clock = clock
        )

        val enabled = wrapper.isEnabled(NavUnleashConfig.Feature.GODSKRIV)
        assertThat(enabled).isTrue()
        assertThat(wrapper.isEnabled(NavUnleashConfig.Feature.GODSKRIV)).isTrue()
        assertThat(wrapper.isEnabled(NavUnleashConfig.Feature.GODSKRIV)).isTrue()
        wrapper.isEnabled(NavUnleashConfig.Feature.GODSKRIV)
        verify(unleash, times(1)).isEnabled(NavUnleashConfig.Feature.GODSKRIV.toggleName)
    }

    @Test
    fun `henter fra unleash hvis verdi ikke lenger er gyldig`() {
        whenever(unleash.isEnabled(any()))
            .thenReturn(true)
            .thenReturn(false)

        val wrapper = UnleashWrapper(
            unleash = unleash,
            clock = clock
        )

        val enabled = wrapper.isEnabled(NavUnleashConfig.Feature.GODSKRIV)
        assertThat(enabled).isTrue()
        clock.nesteTikk(clock.nåtid().plusSeconds(100))
        val notEnabled = wrapper.isEnabled(NavUnleashConfig.Feature.GODSKRIV)
        assertThat(notEnabled).isFalse()
        verify(unleash, times(2)).isEnabled(NavUnleashConfig.Feature.GODSKRIV.toggleName)
    }
}