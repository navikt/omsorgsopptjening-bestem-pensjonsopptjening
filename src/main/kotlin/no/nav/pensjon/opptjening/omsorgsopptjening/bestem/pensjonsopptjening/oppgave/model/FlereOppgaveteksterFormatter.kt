package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

internal data object FlereOppgaveteksterFormatter {
    fun format(oppgavetekster: Set<String>): String {
        return oppgavetekster.joinToString(separator = "\n\n")
    }
}