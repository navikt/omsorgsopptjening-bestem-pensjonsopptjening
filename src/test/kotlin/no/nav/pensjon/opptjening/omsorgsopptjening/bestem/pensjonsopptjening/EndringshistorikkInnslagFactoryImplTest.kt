package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.HandlerMapping
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EndringshistorikkInnslagFactoryImplTest {
    private val tokenValidationContextHolder: TokenValidationContextHolder = mock()
    private val tokenValidationContext: TokenValidationContext = mock()

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `lager innslag med intern bruker og request-metadata`() {
        val request = MockHttpServletRequest("POST", "/api/omsorgsopptjening/123")
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/omsorgsopptjening/{id}")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("NAVident", "Z123456")
                    .claim("sub", "subject")
                    .claim("iss", "issuer")
                    .claim("azp", "azp-verdi")
                    .claim("azp_name", "lesbart-klientnavn")
                    .claim("appid", "appid-verdi")
                    .claim("oid", "oid-verdi")
                    .build()
            )
        )

        val sut = EndringshistorikkInnslagFactoryImpl(
            appVersion = "git_main_ab12cd3",
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create(EndringshistorikkInnslagFactory.Input("Manuell korrigering"))

        assertThat(innslag.utfortAv).isEqualTo("Z123456")
        assertThat(innslag.tidspunkt).isEqualTo(FAST_INSTANT)
        assertThat(innslag.begrunnelse).isEqualTo("Manuell korrigering")
        assertThat(innslag.requestPath).isEqualTo("/api/omsorgsopptjening/{id}")
        assertThat(innslag.requestMethod).isEqualTo("POST")
        assertThat(innslag.tokenInfo).isEqualTo(
            TokenInfo(
                issuer = "issuer",
                subject = "subject",
                azp = "azp-verdi",
                azpName = "lesbart-klientnavn",
                appId = "appid-verdi",
                oid = "oid-verdi",
                navIdent = "Z123456",
            )
        )
        assertThat(innslag.appVersjon).isEqualTo("git_main_ab12cd3")
    }

    @Test
    fun `lager innslag for tjenestekall uten request-context`() {
        val sut = EndringshistorikkInnslagFactoryImpl(
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create()

        assertThat(innslag.utfortAv).isEqualTo("SYSTEM")
        assertThat(innslag.tidspunkt).isEqualTo(FAST_INSTANT)
        assertThat(innslag.requestPath).isNull()
        assertThat(innslag.requestMethod).isNull()
        assertThat(innslag.tokenInfo).isNull()
        assertThat(innslag.appVersjon).isEqualTo("unknown")
    }

    @Test
    fun `bruker NAIS_APP_NAME som utfortAv for tjenestekall uten request-context`() {
        val sut = EndringshistorikkInnslagFactoryImpl(
            naisAppName = "omsorgsopptjening-bestem-pensjonsopptjening",
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create()

        assertThat(innslag.utfortAv).isEqualTo("omsorgsopptjening-bestem-pensjonsopptjening")
    }

    @Test
    fun `bruker azp som utfortAv naar NAVident mangler`() {
        val request = MockHttpServletRequest("POST", "/api/omsorgsopptjening/123")
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/omsorgsopptjening/{id}")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("azp", "4cc7037f-bdcf-48cf-9f4f-4f66e07eb66e")
                    .claim("appid", "e35e9f9c-25de-4a77-bf7f-a2bd95f084f5")
                    .build()
            )
        )

        val sut = EndringshistorikkInnslagFactoryImpl(
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create()

        assertThat(innslag.utfortAv).isEqualTo("4cc7037f-bdcf-48cf-9f4f-4f66e07eb66e")
        assertThat(innslag.tokenInfo?.navIdent).isNull()
        assertThat(innslag.tokenInfo?.azp).isEqualTo("4cc7037f-bdcf-48cf-9f4f-4f66e07eb66e")
        assertThat(innslag.tokenInfo?.appId).isEqualTo("e35e9f9c-25de-4a77-bf7f-a2bd95f084f5")
    }

    @Test
    fun `bruker appid som utfortAv naar NAVident og azp mangler eller er blanke`() {
        val request = MockHttpServletRequest("POST", "/api/omsorgsopptjening/123")
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/omsorgsopptjening/{id}")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("NAVident", "")
                    .claim("appid", "service-app-id")
                    .claim("azp", "")
                    .claim("sub", "service-subject")
                    .build()
            )
        )

        val sut = EndringshistorikkInnslagFactoryImpl(
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create()

        assertThat(innslag.utfortAv).isEqualTo("service-app-id")
    }

    @Test
    fun `bruker sub som utfortAv naar NAVident azp og appid mangler eller er blanke`() {
        val request = MockHttpServletRequest("POST", "/api/omsorgsopptjening/123")
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/omsorgsopptjening/{id}")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("NAVident", "")
                    .claim("appid", " ")
                    .claim("azp", "")
                    .claim("sub", "service-subject")
                    .build()
            )
        )

        val sut = EndringshistorikkInnslagFactoryImpl(
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create()

        assertThat(innslag.utfortAv).isEqualTo("service-subject")
    }

    @Test
    fun `bruker UKJENT naar NAVident azp appid og sub mangler eller er blanke`() {
        val request = MockHttpServletRequest("POST", "/api/omsorgsopptjening/123")
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/omsorgsopptjening/{id}")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("NAVident", "")
                    .claim("appid", " ")
                    .claim("azp", "")
                    .claim("sub", "")
                    .build()
            )
        )

        val sut = EndringshistorikkInnslagFactoryImpl(
            clock = FAST_CLOCK,
            tokenValidationContextHolder = tokenValidationContextHolder,
        )

        val innslag = sut.create()

        assertThat(innslag.utfortAv).isEqualTo("UKJENT")
    }

    private companion object {
        val FAST_INSTANT: Instant = Instant.parse("2026-11-18T10:30:45Z")
        val FAST_CLOCK: Clock = Clock.fixed(FAST_INSTANT, ZoneOffset.UTC)
    }
}
