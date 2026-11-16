package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EndringsloggModelTest {

    @Test
    fun `resultat tillater terminale statuser`() {
        val tillatteStatuser = listOf(
            Endringslogg.Status.OK,
            Endringslogg.Status.INGEN_ENDRING,
            Endringslogg.Status.STOPPET,
            Endringslogg.Status.AVSLUTTET,
            Endringslogg.Status.RESTARTET,
            Endringslogg.Status.IKKE_FUNNET,
            Endringslogg.Status.IKKE_FUNNET_REMOTE,
            Endringslogg.Status.KANSELLERT,
            Endringslogg.Status.FEILET,
        )

        tillatteStatuser.forEach { status ->
            assertThatCode {
                Endringslogg.Resultat(status = status)
            }.doesNotThrowAnyException()
        }
    }

    @Test
    fun `resultat tillater ikke intent-logget`() {
        assertThatThrownBy {
            Endringslogg.Resultat(status = Endringslogg.Status.INTENT_LOGGET)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("terminal status")
    }

}
