package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.omsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.PersonOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.PersonUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class FastsettOmsorgsOpptjening private constructor() {
    companion object {
        fun fastsettOmsorgsOpptjening(
            snapshot: OmsorgsarbeidsSnapshot,
            omsorgsGiver: Person,
            omsorgsMottakere: List<Person>
        ): OmsorgsOpptjening {
            val vilkarsVurdering =
                og(
                    PersonOver16Ar().vilkarsVurder(
                        PersonOgOmsorgsAr(
                            person = omsorgsGiver,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                    PersonUnder70Ar().vilkarsVurder(
                        PersonOgOmsorgsAr(
                            person = omsorgsGiver,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                    eller(
                        omsorgsMottakere.map {
                            HalvtArMedOmsorgForBarnUnder6().vilkarsVurder(
                                HalvtArMedOmsorgGrunnlag(
                                    omsorgsArbeid = snapshot.omsorgsArbeid(omsorgsGiver, it),
                                    omsorgsMottaker = it,
                                    omsorgsAr = snapshot.omsorgsAr
                                )
                            )
                        }
                    )
                )

            val vilkarsResultat = vilkarsVurdering.utfor()

            return OmsorgsOpptjening(
                omsorgsAr = snapshot.omsorgsAr,
                person = omsorgsGiver,
                omsorgsMottakere = omsorgsMottakere,
                grunnlag = snapshot,
                omsorgsopptjeningResultater = vilkarsResultat,
                invilget = vilkarsResultat.avgjorelse
            )
        }
    }
}