package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidsUtbetalinger

data class OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
    val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalinger>,
    val omsorgsAr: Int
)