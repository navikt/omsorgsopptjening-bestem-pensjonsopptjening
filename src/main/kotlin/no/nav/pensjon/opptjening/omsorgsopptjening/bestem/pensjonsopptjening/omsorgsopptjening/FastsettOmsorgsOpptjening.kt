package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.hentOmsorgForBarnUnder6VilkarsVurderinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og

class FastsettOmsorgsOpptjening private constructor() {
    companion object {
        fun fastsettOmsorgsOpptjening(snapshot: OmsorgsarbeidSnapshot): OmsorgsOpptjening {
            val omsorgsgiver = snapshot.omsorgsyter
            val omsorgsmottakere = snapshot.getOmsorgsmottakere(omsorgsgiver)

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
                        omsorgsmottakere.map { omsorgsmottaker ->
                            OmsorgForBarnUnder6().vilkarsVurder(
                                GrunnlagOmsorgForBarnUnder6(
                                    omsorgsArbeid = snapshot.omsorgsarbeidPeriode(omsorgsgiver, omsorgsmottaker),
                                    omsorgsmottaker = omsorgsmottaker,
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
                omsorgsmottakereInvilget = hentOmsorgForBarnUnder6VilkarsVurderinger(vilkarsVurdering)
                    .filter { it.utfall == Utfall.INVILGET }
                    .map { it.grunnlag.omsorgsmottaker }
            )
        }
    }
}