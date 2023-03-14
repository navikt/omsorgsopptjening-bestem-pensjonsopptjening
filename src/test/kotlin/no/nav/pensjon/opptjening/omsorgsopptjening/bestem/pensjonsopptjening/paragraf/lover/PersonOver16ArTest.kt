package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PersonOver16ArTest {
    @ParameterizedTest
    @CsvSource(
        "2000, 2014, AVSLAG",
        "2000, 2015, AVSLAG",
        "2000, 2016, AVSLAG",
        "2000, 2017, INVILGET",
        "2000, 2018, INVILGET"
    )
    fun `Given a person older than 16 years When conducting vilkars vurdering person over 16 Then INVILGET`(
        fodselsAr: Int,
        omsorgsAr: Int,
        avgjorelseInvilget: Avgjorelse
    ) {
        val resultat = PersonOver16Ar()
            .vilkarsVurder(PersonOgOmsorgsAr(Person(fodselsAr = fodselsAr), omsorgsAr))
            .utfor()

        assertEquals(avgjorelseInvilget, resultat.avgjorelse)
    }
}