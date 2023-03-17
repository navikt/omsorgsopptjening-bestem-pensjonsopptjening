package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.omsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.hentHalvtArMedOmsorgVilkarsVurderinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class FastsettOmsorgsOpptjening private constructor() {
    companion object {
        fun fastsettOmsorgsOpptjening(
            snapshot: OmsorgsarbeidsSnapshot,
            omsorgsgiver: Person,
            omsorgsMottakere: List<Person>
        ): OmsorgsOpptjening {
            val vilkarsVurdering =
                og(
                    OmsorgsgiverOver16Ar().vilkarsVurder(
                        OmsorgsGiverOgOmsorgsAr(
                            omsorgsgiver = omsorgsgiver,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                    OmsorgsgiverUnder70Ar().vilkarsVurder(
                        OmsorgsGiverOgOmsorgsAr(
                            omsorgsgiver = omsorgsgiver,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    ),
                    eller(
                        omsorgsMottakere.map {
                            OmsorgForBarnUnder6().vilkarsVurder(
                                GrunnlagOmsorgForBarnUnder6(
                                    omsorgsArbeid = snapshot.omsorgsArbeid(omsorgsgiver, it),
                                    omsorgsMottaker = it,
                                    omsorgsAr = snapshot.omsorgsAr
                                )
                            )
                        }
                    )
                )

            return OmsorgsOpptjening(
                omsorgsAr = snapshot.omsorgsAr,
                person = omsorgsgiver,
                grunnlag = snapshot,
                omsorgsopptjeningResultater = vilkarsVurdering,
                utfall = vilkarsVurdering.utfall,
                omsorgsmottakereInvilget = hentHalvtArMedOmsorgVilkarsVurderinger(vilkarsVurdering)
                    .filter { it.utfall == Utfall.INVILGET }
                    .map { it.grunnlag.omsorgsMottaker }
            )
        }
    }
}