package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag

interface OmsorgsopptjeningsgrunnlagService {
    fun lagOmsorgsopptjeningsgrunnlag(melding: PersongrunnlagMelding.Mottatt): List<OmsorgsopptjeningGrunnlag>
}