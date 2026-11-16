package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder

@Component
class NavIdentProvider(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) {
    fun harRequestContext(): Boolean {
        return RequestContextHolder.getRequestAttributes() != null
    }

    fun getNavIdent(): String? {
        if (!harRequestContext()) return null

        val tokenValidationContext = try {
            tokenValidationContextHolder.getTokenValidationContext()
        } catch (_: IllegalArgumentException) {
            return null
        }

        return tokenValidationContext
            .getClaims(AZURE_ISSUER)
            .getStringClaim(NAV_IDENT_CLAIM)
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {
        const val AZURE_ISSUER = "azure"
        const val NAV_IDENT_CLAIM = "NAVident"
    }
}
