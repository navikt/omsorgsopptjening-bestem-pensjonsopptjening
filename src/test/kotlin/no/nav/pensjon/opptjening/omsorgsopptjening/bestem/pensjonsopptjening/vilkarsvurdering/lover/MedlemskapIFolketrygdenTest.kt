package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagMedlemskapIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MedlemskapIFolketrygdenTest {

    @ParameterizedTest
    @CsvSource(
        "NASJONAL, true, false, INVILGET",
        "NASJONAL, false, false, SAKSBEHANDLING",
        "EOS, false, false, INVILGET",
        "EOS, false, true, SAKSBEHANDLING")
    fun `Given at least 1 months of omsorgsarbeid when child is 0 year old Then omsorg is INVILGET`(
        landstilknytning: Landstilknytning,
        erBosattNorge: Boolean,
        harLopendePensjonUfore: Boolean,
        expectedUtfall: Utfall
    ) {
        val vilkarsVurdering = MedlemskapIFolketrygden().vilkarsVurder(
            grunnlag = GrunnlagMedlemskapIFolketrygden(landstilknytning, erBosattNorge,harLopendePensjonUfore)
        )

        assertEquals(expectedUtfall, vilkarsVurdering.utfall)
    }

}