package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class PersonTest {

    private val aprilNittenÅttiFem = LocalDate.of(1985, Month.APRIL, 1)
    private val nittenfemtiseks = LocalDate.of(1956, Month.JANUARY, 1)

    private val personAprilNittenÅttiFem = Person(
        fnr = "01048512345",
        fødselsdato = aprilNittenÅttiFem,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList())
    )
    private val personNittenFemtiSeks = Person(
        fnr = "12345678910",
        fødselsdato = nittenfemtiseks,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList())
    )

    @Test
    fun alder() {
        personAprilNittenÅttiFem.let {
            assertEquals(-1, it.alderVedUtløpAv(1984))
            assertEquals(0, it.alderVedUtløpAv(1985))
            assertEquals(1, it.alderVedUtløpAv(1986))
        }
    }

    @Test
    fun erFødt() {
        personAprilNittenÅttiFem.let {
            assertEquals(false, it.erFødt(1984))
            assertEquals(true, it.erFødt(1985))
            assertEquals(false, it.erFødt(1986))

            assertEquals(false, it.erFødt(1985, Month.MARCH))
            assertEquals(true, it.erFødt(1985, Month.APRIL))
            assertEquals(false, it.erFødt(1985, Month.MAY))
        }
    }

    @Test
    fun fødselsdato() {
        personAprilNittenÅttiFem.let {
            assertEquals(LocalDate.of(1985, Month.APRIL, 1), it.fødselsdato())
        }
    }


    @Test
    fun `Gitt en person med lik fnr så skal equals være true`() {
        assertEquals(
            personNittenFemtiSeks,
            Person("12345678910", nittenfemtiseks, null, Familierelasjoner(emptyList()))
        )
    }

    @Test
    fun `Gitt samme person objekt så skal equals være true`() {
        assertEquals(personNittenFemtiSeks, personNittenFemtiSeks)
    }

    @Test
    fun `Gitt en person med samme fødsels nummer men annet fødelsdato enn person 2 så skal equals være true`() {
        assertEquals(
            personNittenFemtiSeks,
            Person("12345678910", nittenfemtiseks.plusYears(1), null, Familierelasjoner(emptyList()))
        )
    }

    @Test
    fun `Gitt en person med annet fødsels nummer enn person 2 så skal equals være false`() {
        assertNotEquals(
            personNittenFemtiSeks,
            Person("12345678911", nittenfemtiseks, null, Familierelasjoner(emptyList()))
        )
    }

    @Test
    fun `Gitt en person equals null skal equals være false`() {
        assertNotEquals(personNittenFemtiSeks, null)
    }

    @Test
    fun `Gitt to personer med samme fnr så skal hashcode være lik`() {
        assertEquals(
            personNittenFemtiSeks.hashCode(),
            personNittenFemtiSeks.hashCode()
        )
    }
}