package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag

interface MedlemskapOppslag {
    fun hentMedlemskapsgrunnlag(
        fnr: String
    ): Medlemskapsgrunnlag
}