package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.HandlerMapping
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EndringshistorikkInnslagFactoryImplTest {

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `lager innslag med intern bruker og request-metadata`() {
        val request = MockHttpServletRequest("POST", "/api/omsorgsopptjening/123")
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/omsorgsopptjening/{id}")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        val sut = EndringshistorikkInnslagFactoryImpl(
            utfortAvProvider = FakeUtfortAvProvider(UtfortAvProvider.UtfortAv.InternBruker("Z123456")),
            appVersion = "git_main_ab12cd3",
            clock = FAST_CLOCK,
        )

        val innslag = sut.createInnslag(EndringshistorikkInnslagFactory.Input("Manuell korrigering"))

        assertThat(innslag.utfortAv).isEqualTo("Z123456")
        assertThat(innslag.tidspunkt).isEqualTo(FAST_INSTANT)
        assertThat(innslag.begrunnelse).isEqualTo("Manuell korrigering")
        assertThat(innslag.requestPath).isEqualTo("/api/omsorgsopptjening/{id}")
        assertThat(innslag.requestMethod).isEqualTo("POST")
        assertThat(innslag.appVersjon).isEqualTo("git_main_ab12cd3")
    }

    @Test
    fun `lager innslag for tjenestekall uten request-context`() {
        val sut = EndringshistorikkInnslagFactoryImpl(
            utfortAvProvider = FakeUtfortAvProvider(
                UtfortAvProvider.UtfortAv.Tjeneste("dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening")
            ),
            clock = FAST_CLOCK,
        )

        val innslag = sut.createInnslag()

        assertThat(innslag.utfortAv).isEqualTo("dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening")
        assertThat(innslag.tidspunkt).isEqualTo(FAST_INSTANT)
        assertThat(innslag.requestPath).isNull()
        assertThat(innslag.requestMethod).isNull()
        assertThat(innslag.appVersjon).isEqualTo("unknown")
    }

    private class FakeUtfortAvProvider(
        private val utfortAv: UtfortAvProvider.UtfortAv,
    ) : UtfortAvProvider {
        override fun current(): UtfortAvProvider.UtfortAv = utfortAv

        override fun requireInternBruker(): String {
            return when (utfortAv) {
                is UtfortAvProvider.UtfortAv.InternBruker -> utfortAv.verdi
                else -> error("Ikke intern bruker i denne testen")
            }
        }
    }

    private companion object {
        val FAST_INSTANT: Instant = Instant.parse("2026-11-18T10:30:45Z")
        val FAST_CLOCK: Clock = Clock.fixed(FAST_INSTANT, ZoneOffset.UTC)
    }
}
