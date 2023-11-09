package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

internal data object FlereOppgaveteksterFormatter {
    fun format(oppgavetekster: Set<String>): String {
        return if (oppgavetekster.isNotEmpty()) {
            oppgavetekster.reduce { acc, s -> acc + "\n\n$s" }
        } else {
            ""
        }
    }
}