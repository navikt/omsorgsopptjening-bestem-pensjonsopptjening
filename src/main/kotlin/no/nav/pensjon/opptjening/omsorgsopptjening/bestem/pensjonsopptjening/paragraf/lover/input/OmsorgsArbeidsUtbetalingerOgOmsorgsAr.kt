package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid

data class OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
    val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeid>,
    val omsorgsAr: Int
)