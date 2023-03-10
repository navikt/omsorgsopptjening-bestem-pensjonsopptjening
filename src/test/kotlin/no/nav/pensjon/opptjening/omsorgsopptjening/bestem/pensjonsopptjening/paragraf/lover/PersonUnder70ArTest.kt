package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PersonUnder70ArTest {
    @ParameterizedTest
    @CsvSource(
        "2000, 2071, AVSLAG",
        "2000, 2070, AVSLAG",
        "2000, 2069, INVILGET",
        "2000, 2068, INVILGET",
        "2000, 2067, INVILGET"
    )
    fun `Given a person younger than 70 years When conducting vilkars vurdering Then INVILGET`(
        fodselsAr: Int,
        omsorgsAr: Int,
        expectedAvgjorelse: Avgjorelse
    ) {
        val resultat = PersonUnder70Ar()
            .vilkarsVurder(PersonOgOmsorgsAr(Person(fodselsAr = fodselsAr), omsorgsAr))
            .utforVilkarsVurdering()

        assertEquals(expectedAvgjorelse, resultat.avgjorelse)
    }
}