package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.finnOmsorgsArbeidsUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.Over16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.Under70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsArbeidsUtbetalingerOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class FastsettOmsorgsOpptjening {
    companion object {
        fun fastsettOmsorgsOpptjening(snapshot: OmsorgsarbeidsSnapshot, person: Person): OmsorgsOpptjening {
            val vilkarsResultat =
                og(
                    Over16Ar().vilkarsVurder(
                        PersonOgOmsorgsAr(person = person, omsorgsAr = snapshot.omsorgsAr)
                    ),
                    Under70Ar().vilkarsVurder(
                        PersonOgOmsorgsAr(person = person, omsorgsAr = snapshot.omsorgsAr)
                    ),
                    HalvtArMedOmsorg().vilkarsVurder(
                        OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                            omsorgsArbeidsUtbetalinger = snapshot.finnOmsorgsArbeidsUtbetalinger(person),
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                )

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