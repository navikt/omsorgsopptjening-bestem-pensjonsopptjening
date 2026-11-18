package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class NavIdentProviderTest {

    @Test
    fun `henter NAVident fra simulert JWT-token`() {
        val navIdent = "Z123456"
        val mockOAuth2Server = MockOAuth2Server()
        val token = try {
            mockOAuth2Server.start()
            mockOAuth2Server.issueToken(
                issuerId = "azure",
                subject = "subject",
                audience = "audience",
                claims = mapOf("NAVident" to navIdent),
            ).serialize()
        } finally {
            mockOAuth2Server.shutdown()
        }

        val tokenValidationContext = TokenValidationContext(
            mapOf("azure" to JwtToken(token))
        )
        val contextHolder = StubTokenValidationContextHolder(tokenValidationContext)
        val navIdentProvider = NavIdentProvider(contextHolder)

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        try {
            assertThat(navIdentProvider.getNavIdent()).isEqualTo(navIdent)
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }

    private class StubTokenValidationContextHolder(
        private var tokenValidationContext: TokenValidationContext,
    ) : TokenValidationContextHolder {
        override fun getTokenValidationContext(): TokenValidationContext = tokenValidationContext

        override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
            this.tokenValidationContext = requireNotNull(tokenValidationContext)
        }
    }
}
