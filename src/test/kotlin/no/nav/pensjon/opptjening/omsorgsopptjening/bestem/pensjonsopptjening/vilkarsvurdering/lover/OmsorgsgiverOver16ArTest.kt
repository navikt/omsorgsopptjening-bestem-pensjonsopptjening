package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OmsorgsgiverOver16ArTest {
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
        utfallInvilget: Utfall
    ) {
        val vilkarsVurdering = OmsorgsgiverOver16Ar().vilkarsVurder(OmsorgsGiverOgOmsorgsAr(Person(fodselsAr = fodselsAr), omsorgsAr))

        assertEquals(utfallInvilget, vilkarsVurdering.utfall)
    }
}