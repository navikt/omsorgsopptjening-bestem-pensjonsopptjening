package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PersonOver16ArTest {
    @ParameterizedTest
    @CsvSource(
        "2000, 2014, false",
        "2000, 2015, false",
        "2000, 2016, false",
        "2000, 2017, true",
        "2000, 2018, true"
    )
    fun `Given a person older than 16 years When conducting vilkars vurdering person over 16 Then true`(
        fodselsAr: Int,
        omsorgsAr: Int,
        expectedInvilget: Boolean
    ) {
        val resultat = PersonOver16Ar()
            .vilkarsVurder(PersonOgOmsorgsAr(Person(fodselsAr = fodselsAr), omsorgsAr))
            .utførVilkarsVurdering()

        assertEquals(expectedInvilget, resultat.oppFyllerRegel)
    }
}