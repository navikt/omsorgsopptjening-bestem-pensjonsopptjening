package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidsUtbetalinger

fun List<OmsorgsArbeidsUtbetalinger>.getAntallUtbetalingMoneder(omsorgsAr: Int) =
    (getUtbetalingMoneder() begrensTilAr omsorgsAr).antall()

private fun List<OmsorgsArbeidsUtbetalinger>.getUtbetalingMoneder(): UtbetalingMoneder =
    map { UtbetalingMoneder.UtbetalingMoneder(it.fom, it.tom) }
        .fold(initial = UtbetalingMoneder.emptyUtbetalingMoneder()) { acc, moneder -> acc + moneder }