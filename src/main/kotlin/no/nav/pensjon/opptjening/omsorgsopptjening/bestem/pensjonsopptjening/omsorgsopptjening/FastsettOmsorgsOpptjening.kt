package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.omsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.omsorgsArbeidsUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.PersonOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.PersonUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsArbeidsUtbetalingerOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class FastsettOmsorgsOpptjening private constructor() {
    companion object {
        fun fastsettOmsorgsOpptjening(snapshot: OmsorgsarbeidsSnapshot, person: Person): OmsorgsOpptjening {
            val vilkarsResultat =
                og(
                    PersonOver16Ar().vilkarsVurder(
                        PersonOgOmsorgsAr(
                            person = person,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                    PersonUnder70Ar().vilkarsVurder(
                        PersonOgOmsorgsAr(
                            person = person,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                    HalvtArMedOmsorg().vilkarsVurder(
                        OmsorgsArbeidsUtbetalingerOgOmsorgsAr(
                            omsorgsArbeidsUtbetalinger = snapshot.omsorgsArbeid(person).omsorgsArbeidsUtbetalinger(),
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    )
                )

            return OmsorgsOpptjening(
                omsorgsAr = snapshot.omsorgsAr,
                person = person,
                grunnlag = snapshot,
                omsorgsopptjeningResultater = vilkarsResultat,
                invilget = vilkarsResultat.avgjorelse
            )
        }
    }
}