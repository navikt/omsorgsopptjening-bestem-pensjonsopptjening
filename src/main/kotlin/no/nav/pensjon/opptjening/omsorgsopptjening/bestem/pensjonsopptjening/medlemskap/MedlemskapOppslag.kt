package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Medlemskap

interface MedlemskapOppslag {
    fun hentMedlemskap(fnr: String): Medlemskap
}