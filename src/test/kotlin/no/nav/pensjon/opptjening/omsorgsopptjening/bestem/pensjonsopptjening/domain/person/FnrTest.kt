package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FnrTest {

    @Test
    fun `Given two equal fnr numbers When calling equals than return true`() {
        assertEquals( Fnr("12345678901"), Fnr("12345678901"))
    }

    @Test
    fun `Given same object When calling equals than return true`() {
        val fnr = Fnr("12345678901")
        assertEquals(fnr, fnr)
    }
}