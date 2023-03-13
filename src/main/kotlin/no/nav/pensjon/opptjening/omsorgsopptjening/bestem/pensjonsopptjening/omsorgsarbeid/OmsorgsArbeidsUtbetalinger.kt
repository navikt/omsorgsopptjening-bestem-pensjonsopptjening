package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid

fun List<OmsorgsArbeid>.getAntallUtbetalingMoneder(omsorgsAr: Int) = (getUtbetalingMoneder() begrensTilAr omsorgsAr).antall()

fun List<OmsorgsArbeid>.getUtbetalingMoneder(): UtbetalingMoneder {
    val alleUtbetalingsMoneder = map{UtbetalingMoneder(it.fom, it.tom)}
    return alleUtbetalingsMoneder.fold(initial = UtbetalingMoneder()) { acc, moneder -> acc + moneder }
}