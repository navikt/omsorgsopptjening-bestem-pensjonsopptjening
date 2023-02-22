package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import java.time.LocalDateTime

const val FOLKEREGISTERET = "FREG"

fun List<Foedsel>.avklarFoedsel(): Foedsel? {
    val foedslerSortert = sortedByDescending { it.sisteEndringstidspunktOrNull() }
    val foedselFreg = foedslerSortert.find { it.metadata harMaster FOLKEREGISTERET }
    if (foedselFreg != null) return foedselFreg
    return foedslerSortert.firstOrNull()
}

private fun Foedsel.sisteEndringstidspunktOrNull() = sisteEndringstidspunktOrNull(metadata, folkeregistermetadata)

private fun sisteEndringstidspunktOrNull(metadata: Metadata, fregMetadata: Folkeregistermetadata?): LocalDateTime? =
    when {
        metadata harMaster FOLKEREGISTERET -> fregMetadata?.ajourholdstidspunkt
        else -> metadata.endringer.map { it.registrert }.maxByOrNull { it }
    }

private infix fun Metadata.harMaster(master: String) = master.uppercase() == master
