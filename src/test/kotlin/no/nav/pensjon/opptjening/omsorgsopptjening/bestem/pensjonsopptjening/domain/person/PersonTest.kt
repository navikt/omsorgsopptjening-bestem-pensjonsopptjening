package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PersonFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {

    @Test
    fun `Given fnr matches gjeldende fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = PersonFactory.createPerson("11111111111", 1990)
        val fnr = Fnr("11111111111")

        assertTrue(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr matches gjeldende fnr and there exsists historiske fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )
        val fnr = Fnr("11111111111")

        assertTrue(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr matches one historisk fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )
        val fnr = Fnr("2222222222")

        assertTrue(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr does not matches gjeldende fnr When calling isIdentifiedBy Then return false`() {
        val person: Person = PersonFactory.createPerson("11111111111", 1988)
        val fnr = Fnr("33333333333")

        assertFalse(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr does not matches gjeldende fnr or historiske fnr When calling isIdentifiedBy Then return false`() {
        val person: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )
        val fnr = Fnr("4444444444")

        assertFalse(person identifiseresAv fnr)
    }

    @Test
    fun `Given persons have identical fnrs When calling isSamePerson Then return true`(){
        val person1: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given gjeldendeFnr intersect with historiskeFnr of another person When calling isSamePerson Then return true`(){
        val person1: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = PersonFactory.createPerson(
            gjeldendeFnr = "444444444444",
            historiskeFnr = listOf("11111111111"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given gjeldendeFnr intersects When calling isSamePerson Then return true`(){
        val person1: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("444444444444"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given one historisk fnr intersects When calling isSamePerson Then return true`(){
        val person1: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = PersonFactory.createPerson(
            gjeldendeFnr = "444444444444",
            historiskeFnr = listOf("5555555555", "3333333333"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given no fnrs intersect When calling isSamePerson Then return false`(){
        val person1: Person = PersonFactory.createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = PersonFactory.createPerson(
            gjeldendeFnr = "444444444444",
            historiskeFnr = listOf("5555555555", "66666666666"),
            fodselsAr = 1988
        )

        assertFalse(person1 erSammePerson person2)
    }
}