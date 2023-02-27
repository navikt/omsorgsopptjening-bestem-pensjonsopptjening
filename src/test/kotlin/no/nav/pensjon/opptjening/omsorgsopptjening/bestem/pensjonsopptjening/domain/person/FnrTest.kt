package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

internal class FnrTest {

    @Test
    fun `Given two equal fnr numbers When calling equals than return true`() {
        assertEquals(Fnr(fnr = "12345678901"), Fnr(fnr = "12345678901"))
    }

    @Test
    fun `Given same object When calling equals than return true`() {
        val fnr = Fnr(fnr = "12345678901")
        assertEquals(fnr, fnr)
    }

    @Test
    fun `Given two different fnr When calling equals than return false`() {
        assertNotEquals(Fnr(fnr = "1111111111111"), Fnr(fnr = "22222222222"))
    }

    @Test
    fun `Given one fnr is other class When calling equals than return false`() {
        assertFalse(Fnr(fnr = "1111111111111").equals(OtherClass()))
    }

    private class OtherClass
}