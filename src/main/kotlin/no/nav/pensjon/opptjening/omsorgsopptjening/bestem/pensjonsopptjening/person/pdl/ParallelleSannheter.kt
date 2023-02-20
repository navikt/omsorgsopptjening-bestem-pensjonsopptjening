package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import java.time.LocalDate


internal fun List<Foedsel>.avklareParallelleSannheterOrNull(): Foedsel =
    sortedByDescending { endringstidspunktOrNull(it.metadata, it.folkeregistermetadata) }
        .let {
            firstOrNull { it.metadata.masterfolkeregisteret() }?.let { return it }
            first()
        }

private fun endringstidspunktOrNull(metadata: Metadata, fregMetadata: Folkeregistermetadata?): LocalDate? =
    if (metadata.masterfolkeregisteret()) {
        fregMetadata?.ajourholdstidspunkt
    } else {
        metadata.sisteEndringOrNull()
    }

private fun Metadata.masterfolkeregisteret(): Boolean {
    return master.uppercase() == "FREG"
}

private fun Metadata.sisteEndringOrNull(): LocalDate? = endringer.maxByOrNull { it.registrert }?.registrert
