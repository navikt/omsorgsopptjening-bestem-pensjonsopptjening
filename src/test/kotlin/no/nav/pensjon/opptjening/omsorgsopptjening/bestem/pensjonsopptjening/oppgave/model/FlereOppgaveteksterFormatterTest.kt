package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlereOppgaveteksterFormatterTest {
    @Test
    fun `legg på whitespace mellom hver distinkte tekst`() {
        assertThat(
            FlereOppgaveteksterFormatter.format(
                setOf(
                    "oppgavetekst 1",
                    "oppgavetekst 2",
                    "oppgavetekst 3",
                )
            )
        ).isEqualTo(
            """
            oppgavetekst 1
            
            oppgavetekst 2
            
            oppgavetekst 3
        """.trimIndent()
        )
    }

    @Test
    fun `ingen tekster blir tomt`() {
        assertThat(
            FlereOppgaveteksterFormatter.format(
                emptySet()
            )
        ).isEqualTo("")
    }
}