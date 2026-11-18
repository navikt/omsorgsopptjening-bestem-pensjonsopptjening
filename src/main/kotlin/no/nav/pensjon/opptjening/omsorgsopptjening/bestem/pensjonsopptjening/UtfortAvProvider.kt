package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.server.ResponseStatusException

interface UtfortAvProvider {
    fun current(): UtfortAv
    fun requireInternBruker(): String

    sealed interface UtfortAv {
        val verdi: String

        data class InternBruker(override val verdi: String) : UtfortAv {
            init {
                require(verdi.isNotBlank()) { "Nav-ident kan ikke være blank" }
            }
        }

        data object System : UtfortAv {
            override val verdi: String = "SYSTEM"
        }

        data object Ukjent : UtfortAv {
            override val verdi: String = "UKJENT"
        }

        data class Tjeneste(override val verdi: String) : UtfortAv {
            init {
                require(verdi.isNotBlank()) { "Tokenutsteder kan ikke være blank" }
            }
        }
    }
}

@Component
/**
 * Uten request-context (f.eks. bakgrunnsprosessering) returneres `System`.
 * Med request-context uten token-context returneres `Ukjent`.
 * Med token-context returneres `InternBruker(NAVident)` eller `Tjeneste(token-utsteder)`.
 */
class UtfortAvProviderImpl(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) : UtfortAvProvider {
    override fun current(): UtfortAvProvider.UtfortAv {
        if (!harRequestContext()) return UtfortAvProvider.UtfortAv.System

        val claims = try {
            tokenValidationContextHolder
                .getTokenValidationContext()
                .getClaims(AZURE_ISSUER)
        } catch (_: IllegalArgumentException) {
            null
        } ?: return UtfortAvProvider.UtfortAv.Ukjent

        claims.getStringClaim(NAV_IDENT_CLAIM)
            ?.takeIf { it.isNotBlank() }
            ?.let { return UtfortAvProvider.UtfortAv.InternBruker(it) }

        val tokenUtsteder = listOf(AZP_CLAIM, APP_ID_CLAIM, ISSUER_CLAIM)
            .asSequence()
            .mapNotNull { claim -> claims.getStringClaim(claim) }
            .firstOrNull { it.isNotBlank() }
            ?: return UtfortAvProvider.UtfortAv.Ukjent

        return UtfortAvProvider.UtfortAv.Tjeneste(tokenUtsteder)
    }

    override fun requireInternBruker(): String {
        return when (val identitet = current()) {
            is UtfortAvProvider.UtfortAv.InternBruker -> identitet.verdi
            else -> throw ResponseStatusException(HttpStatus.FORBIDDEN, "Mangler NAVident-claim")
        }
    }

    private fun harRequestContext(): Boolean {
        return RequestContextHolder.getRequestAttributes() != null
    }

    private companion object {
        const val AZURE_ISSUER = "azure"
        const val NAV_IDENT_CLAIM = "NAVident"
        const val AZP_CLAIM = "azp"
        const val APP_ID_CLAIM = "appid"
        const val ISSUER_CLAIM = "iss"
    }
}
