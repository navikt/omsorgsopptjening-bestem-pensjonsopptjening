package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot


fun OmsorgsarbeidsSnapshot.omsorgsArbeid(person: Person): List<OmsorgsArbeid> =
    omsorgsArbeidSaker.flatMap { sak ->
        sak.omsorgsarbedUtfort.filter { omsorgsArbeid ->
            person.identifiseresAv(Fnr(fnr = omsorgsArbeid.omsorgsyter.fnr))
        }
    }

fun List<OmsorgsArbeid>.omsorgsArbeidsUtbetalinger() = map { it.omsorgsArbeidsUtbetalinger }