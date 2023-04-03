package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OmsorgsgiverUnder70ArTest {
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
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = OmsorgsgiverUnder70Ar().vilkarsVurder(OmsorgsGiverOgOmsorgsAr(Person(fodselsAr = fodselsAr), omsorgsAr))

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
    }
}