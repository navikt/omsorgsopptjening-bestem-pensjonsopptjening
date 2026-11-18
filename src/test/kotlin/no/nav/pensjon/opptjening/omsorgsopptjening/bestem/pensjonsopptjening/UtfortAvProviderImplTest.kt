package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.server.ResponseStatusException

class UtfortAvProviderImplTest {

    private val tokenValidationContextHolder: TokenValidationContextHolder = mock()
    private val tokenValidationContext: TokenValidationContext = mock()

    private val sut = UtfortAvProviderImpl(tokenValidationContextHolder)

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `current returnerer System uten request-context`() {
        val current = sut.current()

        assertThat(current).isEqualTo(UtfortAvProvider.UtfortAv.System)
    }

    @Test
    fun `current returnerer Ukjent ved request-context uten azure-claims`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenThrow(IllegalArgumentException("mangler issuer"))

        val current = sut.current()

        assertThat(current).isEqualTo(UtfortAvProvider.UtfortAv.Ukjent)
    }

    @Test
    fun `current returnerer InternBruker naar NAVident finnes`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("NAVident", "Z123456")
                    .claim("azp", "dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening")
                    .build()
            )
        )

        val current = sut.current()

        assertThat(current).isEqualTo(UtfortAvProvider.UtfortAv.InternBruker("Z123456"))
    }

    @Test
    fun `current returnerer Tjeneste naar NAVident mangler men azp finnes`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("azp", "dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening")
                    .build()
            )
        )

        val current = sut.current()

        assertThat(current).isEqualTo(
            UtfortAvProvider.UtfortAv.Tjeneste("dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening")
        )
    }

    @Test
    fun `requireInternBruker returnerer NAVident`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(JWTClaimsSet.Builder().claim("NAVident", "A654321").build())
        )

        val navIdent = sut.requireInternBruker()

        assertThat(navIdent).isEqualTo("A654321")
    }

    @Test
    fun `requireInternBruker kaster forbidden naar ikke intern bruker`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        whenever(tokenValidationContextHolder.getTokenValidationContext()).thenReturn(tokenValidationContext)
        whenever(tokenValidationContext.getClaims("azure")).thenReturn(
            JwtTokenClaims(
                JWTClaimsSet.Builder()
                    .claim("appid", "e35e9f9c-25de-4a77-bf7f-a2bd95f084f5")
                    .build()
            )
        )

        assertThatThrownBy { sut.requireInternBruker() }
            .isInstanceOfSatisfying(ResponseStatusException::class.java) { exception ->
                assertThat(exception.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
                assertThat(exception.reason).isEqualTo("Mangler NAVident-claim")
            }
    }
}
