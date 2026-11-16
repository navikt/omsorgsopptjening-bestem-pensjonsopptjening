package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web.NavIdentProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ContextUtfortAvProviderTest {

    private val navIdentProvider = mock<NavIdentProvider>()
    private val utfortAvProvider = ContextUtfortAvProvider(navIdentProvider)

    @Test
    fun `returnerer saksbehandler nar navident finnes`() {
        whenever(navIdentProvider.harRequestContext()).thenReturn(true)
        whenever(navIdentProvider.getNavIdent()).thenReturn("Z12345")

        val result = utfortAvProvider.current()

        assertThat(result).isEqualTo(UtfortAvProvider.UtfortAv.Saksbehandler("Z12345"))
    }

    @Test
    fun `kaster exception nar navident mangler i request context`() {
        whenever(navIdentProvider.harRequestContext()).thenReturn(true)
        whenever(navIdentProvider.getNavIdent()).thenReturn(null)

        assertThatThrownBy { utfortAvProvider.current() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Mangler NAV-ident i request context")
    }

    @Test
    fun `kaster exception nar navident er tom streng i request context`() {
        whenever(navIdentProvider.harRequestContext()).thenReturn(true)
        whenever(navIdentProvider.getNavIdent()).thenReturn("")

        assertThatThrownBy { utfortAvProvider.current() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Mangler NAV-ident i request context")
    }

    @Test
    fun `returnerer system nar request context mangler`() {
        whenever(navIdentProvider.harRequestContext()).thenReturn(false)

        val result = utfortAvProvider.current()

        assertThat(result).isEqualTo(UtfortAvProvider.UtfortAv.System)
    }
}
