package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class PersonTest {

    private val aprilNittenÅttiFem = LocalDate.of(1985, Month.APRIL, 1)
    private val nittenfemtiseks = LocalDate.of(1956, Month.JANUARY, 1)

    @Test
    fun alder() {
        Person(
            fnr = "01048512345",
            fødselsdato = aprilNittenÅttiFem
        ).let {
            assertEquals(-1, it.alderVedUtløpAv(1984))
            assertEquals(0, it.alderVedUtløpAv(1985))
            assertEquals(1, it.alderVedUtløpAv(1986))
        }
    }

    @Test
    fun erFødt() {
        Person(
            fnr = "01048512345",
            fødselsdato = aprilNittenÅttiFem
        ).let {
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
        Person(
            fnr = "01048512345",
            fødselsdato = aprilNittenÅttiFem
        ).let {
            assertEquals(LocalDate.of(1985, Month.APRIL, 1), it.fødselsdato())
        }
    }

    @Test
    fun `Gitt en person med lik fnr så skal equals være true`() {
        assertEquals(Person(fnr = "12345678910", fødselsdato = nittenfemtiseks), Person("12345678910", nittenfemtiseks))
    }

    @Test
    fun `Gitt samme person objekt så skal equals være true`() {
        val person1 = Person(fnr = "12345678910", fødselsdato = nittenfemtiseks)
        assertEquals(person1, person1)
    }

    @Test
    fun `Gitt en person med samme fødsels nummer men annet fødelsdato enn person 2 så skal equals være true`() {
        assertEquals(Person("12345678910", nittenfemtiseks), Person("12345678910", nittenfemtiseks.plusYears(1)))
    }

    @Test
    fun `Gitt en person med annet fødsels nummer enn person 2 så skal equals være false`() {
        assertNotEquals(Person("12345678910", nittenfemtiseks), Person("12345678911", nittenfemtiseks))
    }

    @Test
    fun `Gitt en person equals null skal equals være false`() {
        assertNotEquals(Person("12345678910", nittenfemtiseks), null)
    }

    @Test
    fun `Gitt to personer med samme fnr så skal hashcode være lik`() {
        assertEquals(Person("12345678910", nittenfemtiseks).hashCode(), Person("12345678910", nittenfemtiseks).hashCode())
    }
}