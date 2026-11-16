package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web.NavIdentProvider
import org.springframework.stereotype.Component

interface UtfortAvProvider {
    fun current(): UtfortAv

    sealed interface UtfortAv {
        val verdi: String

        data class Saksbehandler(override val verdi: String) : UtfortAv {
            init {
                require(verdi.isNotBlank()) { "Nav-ident kan ikke være blank" }
            }
        }

        data object System : UtfortAv {
            override val verdi: String = "SYSTEM"
        }
    }
}

@Component
/**
 * Støtter kall med request-context (direkte brukertoken/OBO) når token inneholder `NAVident`.
 * Støtter ikke maskin-til-maskin-token.
 * Uten request-context (f.eks. bakgrunnsprosessering) returneres `System`.
 */
class ContextUtfortAvProvider(
    private val navIdentProvider: NavIdentProvider,
) : UtfortAvProvider {
    override fun current(): UtfortAvProvider.UtfortAv {
        if (!navIdentProvider.harRequestContext()) return UtfortAvProvider.UtfortAv.System
        val navIdent = navIdentProvider.getNavIdent()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Mangler NAV-ident i request context")
        return UtfortAvProvider.UtfortAv.Saksbehandler(navIdent)
    }
}
