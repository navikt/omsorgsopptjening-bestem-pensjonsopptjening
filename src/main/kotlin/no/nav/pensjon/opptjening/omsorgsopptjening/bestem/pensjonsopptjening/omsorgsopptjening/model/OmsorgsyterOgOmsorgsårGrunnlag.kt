package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

data class OmsorgsyterOgOmsorgsårGrunnlag(
    val omsorgsyter: PersonMedFødselsår,
    val omsorgsAr: Int
)