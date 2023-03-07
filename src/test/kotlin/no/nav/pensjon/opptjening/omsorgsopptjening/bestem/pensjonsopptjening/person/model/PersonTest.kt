package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PersonTest {

    @Test
    fun `Given fnr matches gjeldende fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = createPerson("11111111111", 1990)
        val fnr = Fnr(fnr = "11111111111")

        assertTrue(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr matches gjeldende fnr and there exsists historiske fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )
        val fnr = Fnr(fnr = "11111111111")

        assertTrue(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr matches one historisk fnr When calling isIdentifiedBy Then return true`() {
        val person: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )
        val fnr = Fnr(fnr = "2222222222")

        assertTrue(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr does not matches gjeldende fnr When calling isIdentifiedBy Then return false`() {
        val person: Person = createPerson("11111111111", 1988)
        val fnr = Fnr(fnr = "33333333333")

        assertFalse(person identifiseresAv fnr)
    }

    @Test
    fun `Given fnr does not matches gjeldende fnr or historiske fnr When calling isIdentifiedBy Then return false`() {
        val person: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )
        val fnr = Fnr(fnr = "4444444444")

        assertFalse(person identifiseresAv fnr)
    }

    @Test
    fun `Given persons have identical fnrs When calling isSamePerson Then return true`(){
        val person1: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given gjeldendeFnr intersect with historiskeFnr of another person When calling isSamePerson Then return true`(){
        val person1: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = createPerson(
            gjeldendeFnr = "444444444444",
            historiskeFnr = listOf("11111111111"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given gjeldendeFnr intersects When calling isSamePerson Then return true`(){
        val person1: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("444444444444"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given one historisk fnr intersects When calling isSamePerson Then return true`(){
        val person1: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = createPerson(
            gjeldendeFnr = "444444444444",
            historiskeFnr = listOf("5555555555", "3333333333"),
            fodselsAr = 1988
        )

        assertTrue(person1 erSammePerson person2)
    }

    @Test
    fun `Given no fnrs intersect When calling isSamePerson Then return false`(){
        val person1: Person = createPerson(
            gjeldendeFnr = "11111111111",
            historiskeFnr = listOf("2222222222", "3333333333"),
            fodselsAr = 1988
        )

        val person2: Person = createPerson(
            gjeldendeFnr = "444444444444",
            historiskeFnr = listOf("5555555555", "66666666666"),
            fodselsAr = 1988
        )

        assertFalse(person1 erSammePerson person2)
    }

    @Test
    fun `Given no gjeldende fnr in db for person when trying to update gjeldende fnr in DB then insert new fnr to person`(){
        val person = Person()
        assertEquals( 0, person.alleFnr.filter { it.gjeldende }.size)
        person.oppdaterGjeldendeFnr("12345678901")
        assertEquals( 1, person.alleFnr.filter { it.gjeldende }.size)
    }

    @Test
    fun `Given a gjeldende fnr in db for person when trying to update gjeldende fnr in DBthen insert new fnr to person and remove old gjeldende fnr`(){
        val person = Person(alleFnr = mutableSetOf(Fnr(fnr = "12345678901", gjeldende = true)))
        person.oppdaterGjeldendeFnr("12345678902")
        assertEquals( 1, person.alleFnr.filter { it.gjeldende }.size)
        assertEquals("12345678902", person.alleFnr.first().fnr)
    }

    @Test
    fun `Given no historiske fnr in db for person when trying to update historiske fnr in DB then insert new fnr to person`(){
        val person = Person()
        assertEquals( 0, person.alleFnr.filter { !it.gjeldende}.size)
        person.oppdaterHistoriskeFnr(listOf("12345678901"))
        assertEquals( 1, person.alleFnr.filter { !it.gjeldende }.size)
        assertEquals("12345678901", person.historiskeFnr.first().fnr)
    }

    @Test
    fun `Given a historiske fnr in db for person when trying to update historiske fnr in DBthen insert new fnr to person and remove old historiske fnr`(){
        val person = Person(alleFnr = mutableSetOf(Fnr(fnr = "12345678901", gjeldende = false)))
        person.oppdaterHistoriskeFnr(listOf("12345678902"))
        assertEquals( 1, person.alleFnr.filter { !it.gjeldende }.size)
        assertEquals("12345678902", person.alleFnr.first().fnr)
    }

    private fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
        Person(
            alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toMutableSet().apply { add(Fnr(fnr = gjeldendeFnr, gjeldende = true)) },
            fodselsAr = fodselsAr
        )
}