package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PersonUnder70ArTest {
    @ParameterizedTest
    @CsvSource(
        "2000, 2071, false",
        "2000, 2070, false",
        "2000, 2069, true",
        "2000, 2068, true",
        "2000, 2067, true"
    )
    fun `Given a person younger than 70 years When conducting vilkars vurdering person under 70 Then true`(
        fodselsAr: Int,
        omsorgsAr: Int,
        expectedInvilget: Boolean
    ) {
        val resultat = PersonUnder70Ar()
            .vilkarsVurder(PersonOgOmsorgsAr(Person(fodselsAr = fodselsAr), omsorgsAr))
            .utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }
}