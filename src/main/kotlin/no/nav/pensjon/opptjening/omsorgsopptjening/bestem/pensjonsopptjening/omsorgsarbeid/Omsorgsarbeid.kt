package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot


fun OmsorgsarbeidsSnapshot.finnOmsorgsArbeid(person: Person): List<OmsorgsArbeid> =
    omsorgsArbeidSaker.flatMap { sak ->
        sak.omsorgsarbedUtfort.filter { omsorgsArbeid ->
            person.identifiseresAv(Fnr(fnr = omsorgsArbeid.omsorgsyter.fnr))
        }
    }

fun List<OmsorgsArbeid>.getUtbetalinger(omsorgsAr: Int) = (getUtbetalinger() begrensTilAr omsorgsAr).antall()

private fun List<OmsorgsArbeid>.getUtbetalinger() =
    map { utbetalinger -> utbetalinger.utbetalinger() }
        .fold(initial = utbetalinger()) { acc, moneder -> acc + moneder }

fun OmsorgsArbeid.utbetalinger() = utbetalinger(omsorgsArbeidsUtbetalinger.fom, omsorgsArbeidsUtbetalinger.tom)

