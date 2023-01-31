package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory.PersonFactory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {

    @Test
    fun `Given fnr matches gjeldende fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = PersonFactory.createPerson("11111111111")
        val fnr = Fnr("11111111111")

        assertTrue(person isIdentifiedBy fnr)
    }

    @Test
    fun `Given fnr matches gjeldende fnr and there exsists historiske fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333")
        )
        val fnr = Fnr("11111111111")

        assertTrue(person isIdentifiedBy fnr)
    }

    @Test
    fun `Given fnr matches one historisk fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333")
        )
        val fnr = Fnr("2222222222")

        assertTrue(person isIdentifiedBy fnr)
    }

    @Test
    fun `Given fnr does not matches gjeldende fnr When calling isIdentifiedBy Then return false`() {
        val person: Person = PersonFactory.createPerson("11111111111")
        val fnr = Fnr("33333333333")

        assertFalse(person isIdentifiedBy fnr)
    }

    @Test
    fun `Given fnr does not matches gjeldende fnr or historiske fnr When calling isIdentifiedBy Then return false`() {
        val person: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333")
        )
        val fnr = Fnr("4444444444")

        assertFalse(person isIdentifiedBy fnr)
    }
}