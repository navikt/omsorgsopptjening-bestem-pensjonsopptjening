package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.HandlerMapping
import java.time.Clock
import java.time.Instant

data class EndringshistorikkInnslag(
    val utfortAv: String,
    val tidspunkt: Instant,
    val begrunnelse: String? = null,
    val requestPath: String? = null,
    val requestMethod: String? = null,
    val tokenInfo: TokenInfo? = null,
    val appVersjon: String? = null,
) {
    fun serializeSomListe(): String = listOf(this).serializeList()
}

// Prioritering for utfortAv:
// 1) NAVident (kun i brukerflyter: login/OBO; ingen ekstra NAIS-config),
// 2) azp (forventes i Entra v2-token for både bruker- og M2M-flyt; ingen ekstra NAIS-config),
// 3) appid (legacy/fallback, kan forekomme avhengig av token/flyt; normalt ingen ekstra NAIS-config),
// 4) sub (forventes i gyldige access token; ingen ekstra NAIS-config) som siste fallback.
//
// azp brukes som stabil, unik identifikator for sporbarhet/autorisasjon.
// azp_name (default claim; ingen ekstra NAIS-config) lagres i tillegg for lesbarhet, men brukes ikke som
// primær identifikator fordi claimen ikke er garantert unik og kan endres over tid.
//
// Referanser:
// - https://doc.nav.cloud.nais.io/auth/entra-id/reference/ (azp, azp_name, NAVident)
// - https://doc.nais.io/workloads/application/reference/spec/ (azure.application.claims.extra er deprecated/ignorert)
data class TokenInfo(
    val issuer: String? = null,
    val subject: String? = null,
    val azp: String? = null,
    val azpName: String? = null,
    val appId: String? = null,
    val oid: String? = null,
    val navIdent: String? = null,
) {
    fun finnUtfortAv(): String? = sequenceOf(navIdent, azp, appId, subject).firstOrNull { !it.isNullOrBlank() }
}

interface EndringshistorikkInnslagFactory {
    data class Input(
        val begrunnelse: String? = null,
    )

    fun create(input: Input = Input()): EndringshistorikkInnslag
}

@Component
class EndringshistorikkInnslagFactoryImpl(
    @Value($$"${NAIS_APP_IMAGE:unknown}") private val appVersion: String = "unknown",
    @Value($$"${NAIS_APP_NAME:SYSTEM}") private val naisAppName: String = "SYSTEM",
    private val clock: Clock = Clock.systemUTC(),
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) : EndringshistorikkInnslagFactory {

    override fun create(input: EndringshistorikkInnslagFactory.Input): EndringshistorikkInnslag {
        val attributes = getRequestAttributes() ?: return EndringshistorikkInnslag(
            utfortAv = naisAppName,
            tidspunkt = Instant.now(clock),
            begrunnelse = input.begrunnelse,
            appVersjon = appVersion,
        )

        val path = attributes.matchingPath()
        val method = attributes.request.method
        val tokenInfo = hentTokenInfo()

        return EndringshistorikkInnslag(
            utfortAv = tokenInfo?.finnUtfortAv() ?: UKJENT_VERDI,
            tidspunkt = Instant.now(clock),
            begrunnelse = input.begrunnelse,
            requestPath = path,
            requestMethod = method,
            tokenInfo = tokenInfo,
            appVersjon = appVersion,
        )
    }

    private fun hentTokenInfo(): TokenInfo? {
        val claims = try {
            tokenValidationContextHolder
                .getTokenValidationContext()
                .getClaims(AZURE_ISSUER)
        } catch (_: IllegalArgumentException) {
            null
        } ?: return null

        return TokenInfo(
            issuer = claims.getStringClaim(ISSUER_CLAIM),
            subject = claims.getStringClaim(SUBJECT_CLAIM),
            azp = claims.getStringClaim(AZP_CLAIM),
            azpName = claims.getStringClaim(AZP_NAME_CLAIM),
            appId = claims.getStringClaim(APP_ID_CLAIM),
            oid = claims.getStringClaim(OID_CLAIM),
            navIdent = claims.getStringClaim(NAV_IDENT_CLAIM),
        )
    }

    private fun getRequestAttributes() =
        RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes

    // Bruker Spring sitt route-mønster (f.eks. /api/{id}) i stedet for rå request-URI fra brukerinput.
    private fun ServletRequestAttributes.matchingPath(): String? =
        this.request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String

    private companion object {
        const val UKJENT_VERDI = "UKJENT"
        const val AZURE_ISSUER = "azure"
        const val ISSUER_CLAIM = "iss"
        const val SUBJECT_CLAIM = "sub"
        const val AZP_CLAIM = "azp"
        const val AZP_NAME_CLAIM = "azp_name"
        const val APP_ID_CLAIM = "appid"
        const val OID_CLAIM = "oid"
        const val NAV_IDENT_CLAIM = "NAVident"
    }
}
