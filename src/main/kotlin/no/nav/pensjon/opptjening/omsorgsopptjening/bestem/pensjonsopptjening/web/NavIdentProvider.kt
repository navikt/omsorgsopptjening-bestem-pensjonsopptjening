package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder

@Component
class NavIdentProvider(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) {
    fun getNavIdent(): String? {
        if (RequestContextHolder.getRequestAttributes() == null) return null

        return tokenValidationContextHolder
            .getTokenValidationContext()
            .anyValidClaims
            ?.getStringClaim(NAV_IDENT_CLAIM)
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {
        const val NAV_IDENT_CLAIM = "NAVident"
    }
}
