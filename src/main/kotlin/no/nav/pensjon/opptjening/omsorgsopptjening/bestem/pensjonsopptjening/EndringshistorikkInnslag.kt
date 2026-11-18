package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
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
    val appVersjon: String? = null,
) {
    fun serializeSomListe(): String = listOf(this).serializeList()
}

interface EndringshistorikkInnslagFactory {
    data class Input(
        val begrunnelse: String? = null,
    )

    fun create(input: Input = Input()): EndringshistorikkInnslag
}

@Component
class EndringshistorikkInnslagFactoryImpl(
    private val utfortAvProvider: UtfortAvProvider,
    @Value($$"${APP_VERSION:unknown}") private val appVersion: String = "unknown",
    private val clock: Clock = Clock.systemUTC(),
) : EndringshistorikkInnslagFactory {

    override fun create(input: EndringshistorikkInnslagFactory.Input): EndringshistorikkInnslag {
        val attributes = getRequestAttributes()
        val path = attributes?.matchingPath()
        val method = attributes?.request?.method

        return EndringshistorikkInnslag(
            utfortAv = utfortAvProvider.current().verdi,
            tidspunkt = Instant.now(clock),
            begrunnelse = input.begrunnelse,
            requestPath = path,
            requestMethod = method,
            appVersjon = appVersion,
        )
    }

    private fun getRequestAttributes() =
        RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes

    // Bruker Spring sitt route-mønster (f.eks. /api/{id}) i stedet for rå request-URI fra brukerinput.
    private fun ServletRequestAttributes.matchingPath(): String? =
        this.request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
}
