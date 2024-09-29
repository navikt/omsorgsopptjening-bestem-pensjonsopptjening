package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslagException
import org.springframework.stereotype.Service

@Service
internal class PdlService(
    private val pdlClient: PdlClient
) : PersonOppslag {

    override fun hentPerson(fnr: String): Person {
        try {
            val pdlResponse = pdlClient.hentPerson(fnr = fnr)

            val hentPersonQueryResponse = pdlResponse?.data?.hentPerson ?: throw PdlException(pdlResponse?.error)

            return hentPersonQueryResponse.toDomain()
        } catch (ex: Throwable) {
            throw PersonOppslagException("Feil ved henting av person", ex)
        }
    }

    override fun hentAktørId(fnr: String): String {
        try {
            val pdlResponse = pdlClient.hentAktorId(fnr = fnr)
            return pdlResponse?.data?.hentIdenter?.identer
                ?.firstOrNull { it.gruppe == IdentGruppe.AKTORID }
                ?.let { it.ident }
                ?: throw RuntimeException("Fant ingen aktørId")

        } catch (ex: Throwable) {
            throw PersonOppslagException("Feil ved henting av aktørid", ex)
        }
    }
}

internal class PdlException(pdlError: PdlError?) : RuntimeException(pdlError?.message ?: "Unknown error from PDL") {
    val code: PdlErrorCode? = pdlError?.extensions?.code
}

internal class PdlMottatDataException(message: String) : RuntimeException(message)