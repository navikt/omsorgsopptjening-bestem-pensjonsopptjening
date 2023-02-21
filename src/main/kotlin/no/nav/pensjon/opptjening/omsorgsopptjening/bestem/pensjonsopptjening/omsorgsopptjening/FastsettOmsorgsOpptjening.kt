package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.finnOmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.getUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class FastsettOmsorgsOpptjening {
    companion object {
        fun fastsettOmsorgsOpptjening(snapshot: OmsorgsarbeidsSnapshot, person: Person): OmsorgsOpptjening {
            val vilkarsResultat = HalvtArMedOmsorg()
                .vilkarsVurder(
                    snapshot
                        .finnOmsorgsArbeid(person)
                        .getUtbetalinger(snapshot.omsorgsAr)
                )
                .utførVilkarsVurdering()

            return OmsorgsOpptjening(
                omsorgsAr = snapshot.omsorgsAr,
                person = person,
                grunnlag = snapshot,
                omsorgsopptjeningResultater = vilkarsResultat,
                invilget = vilkarsResultat.oppFyllerRegel
            )
        }
    }
}