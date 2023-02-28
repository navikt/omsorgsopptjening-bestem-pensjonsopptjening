package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidsUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot


fun OmsorgsarbeidsSnapshot.finnOmsorgsArbeidsUtbetalinger(person: Person): List<OmsorgsArbeidsUtbetalinger> =
    omsorgsArbeidSaker.flatMap { sak ->
        sak.omsorgsarbedUtfort.filter { omsorgsArbeid ->
            person.identifiseresAv(Fnr(fnr = omsorgsArbeid.omsorgsyter.fnr))
        }
    }.map { it.omsorgsArbeidsUtbetalinger }

fun List<OmsorgsArbeidsUtbetalinger>.getUtbetalinger(omsorgsAr: Int) =
    (getUtbetalinger() begrensTilAr omsorgsAr).antall()

private fun List<OmsorgsArbeidsUtbetalinger>.getUtbetalinger() =
    map { utbetalinger(it.fom, it.tom) }
        .fold(initial = utbetalinger()) { acc, moneder -> acc + moneder }

