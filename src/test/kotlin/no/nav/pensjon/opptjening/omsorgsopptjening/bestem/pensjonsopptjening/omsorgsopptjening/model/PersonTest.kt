package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.Month

class PersonTest {

    private val aprilNittenÅttiFem = LocalDate.of(1985, Month.APRIL, 1)
    private val nittenfemtiseks = LocalDate.of(1956, Month.JANUARY, 1)

    private val personAprilNittenÅttiFem = Person(
        fødselsdato = aprilNittenÅttiFem,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList()),
        identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("01048512345")))
    )
    private val personNittenFemtiSeks = Person(
        fødselsdato = nittenfemtiseks,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList()),
        identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("12345678910")))
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
            Person(
                fødselsdato = nittenfemtiseks,
                dødsdato = null,
                familierelasjoner = Familierelasjoner(emptyList()),
                identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("12345678910")))

            )
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
            Person(
                fødselsdato = nittenfemtiseks.plusYears(1),
                dødsdato = null,
                familierelasjoner = Familierelasjoner(emptyList()),
                identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("12345678910")))
            )
        )
    }

    @Test
    fun `Gitt en person med annet fødsels nummer enn person 2 så skal equals være false`() {
        assertNotEquals(
            personNittenFemtiSeks,
            Person(
                fødselsdato = nittenfemtiseks,
                dødsdato = null,
                familierelasjoner = Familierelasjoner(emptyList()),
                identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("12345678911")))
            )
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

    @Test
    fun `En person kan identifiseres av flere fødselsnummer`() {
        val id1 = "12345678911"
        val id2 = "12345678912"
        val id3 = "bogus"

        val person = Person(
            fødselsdato = nittenfemtiseks,
            dødsdato = null,
            familierelasjoner = Familierelasjoner(emptyList()),
            identhistorikk = IdentHistorikk(
                setOf(
                    Ident.FolkeregisterIdent.Gjeldende(id1),
                    Ident.FolkeregisterIdent.Historisk(id2),
                )
            )
        )

        assertThat(person.identifisertAv(id1)).isTrue()
        assertThat(person.identifisertAv(id2)).isTrue()
        assertThat(person.identifisertAv(id3)).isFalse()
    }

    @Test
    fun `Kaster exception dersom en person har 0 gjeldende identer`() {
        assertThrows<IdentHistorikk.IdentHistorikkManglerGjeldendeException> {
            Person(
                fødselsdato = nittenfemtiseks,
                dødsdato = null,
                familierelasjoner = Familierelasjoner(emptyList()),
                identhistorikk = IdentHistorikk(
                    setOf(
                        Ident.FolkeregisterIdent.Historisk("1"),
                    )
                )
            )
        }
    }

    @Test
    fun `Kaster exception dersom en person har mange gjeldende identer`() {
        assertThrows<IdentHistorikk.IdentHistorikkManglerGjeldendeException> {
            Person(
                fødselsdato = nittenfemtiseks,
                dødsdato = null,
                familierelasjoner = Familierelasjoner(emptyList()),
                identhistorikk = IdentHistorikk(
                    setOf(
                        Ident.FolkeregisterIdent.Gjeldende("1"),
                        Ident.FolkeregisterIdent.Gjeldende("2"),
                    )
                )
            )
        }
    }
}