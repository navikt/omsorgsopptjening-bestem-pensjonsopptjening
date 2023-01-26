package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OmsorgsArbeidFactoryTest{

    @Test
    fun `When calling should OmsorgsArbeidFactory Then create OmsorgsArbeid`() {
        assertNotNull(OmsorgsArbeidFactory.createOmsorgsArbeid())
    }

    @Test
    fun `When using OmsorgsArbeidFactory Then create OmsorgsArbeid`() {
        OmsorgsArbeidFactory.Companion.createOmsorgsArbeid()
        assertNotNull(OmsorgsArbeidFactory.createOmsorgsArbeid())
    }


}