package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsyterModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory.OmsorgsArbeidFactory
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class OmsorgsArbeidFactoryTest {

    @Test
    fun `Given OmsorgsArbeid from model When using OmsorgsArbeidFactory Then create OmsorgsArbeid domain`() {
        val omsorgsArbeidModel = creatOmsorgsArbeidModel()
        assertNotNull(OmsorgsArbeidFactory.createOmsorgsArbeid(omsorgsArbeidModel))
    }

    private fun creatOmsorgsArbeidModel() = OmsorgsArbeidModel(
        omsorgsAr = "2010",
        hash = "12345",
        omsorgsyter = OmsorgsyterModel(
            fnr = "1234566", utbetalingsperioder = listOf()
        )
    )
}