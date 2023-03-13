package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidsUtbetalinger

fun List<OmsorgsArbeidsUtbetalinger>.getAntallUtbetalingMoneder(omsorgsAr: Int) =
    (getUtbetalingMoneder() begrensTilAr omsorgsAr).antall()

fun List<OmsorgsArbeidsUtbetalinger>.getUtbetalingMoneder(): UtbetalingMoneder {
    val alleUtbetalingsMoneder = map{UtbetalingMoneder.UtbetalingMoneder(it.fom, it.tom)}
    return alleUtbetalingsMoneder.fold(initial = UtbetalingMoneder.emptyUtbetalingMoneder()) { acc, moneder -> acc + moneder }
}