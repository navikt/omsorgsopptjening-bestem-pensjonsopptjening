package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Eller.Companion.minstEn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og

class VilkarsvurderOmsorgsOpptjening private constructor() {
    companion object {
        fun vilkarsvurder(snapshot: OmsorgsarbeidSnapshot): VilkarsVurdering<*> {
            val omsorgsyter = snapshot.omsorgsyter
            val omsorgsmottakere = snapshot.getOmsorgsmottakere(omsorgsyter)

            return og(
                OmsorgsgiverOver16Ar().vilkarsVurder(
                    OmsorgsGiverOgOmsorgsAr(
                        omsorgsgiver = omsorgsyter,
                        omsorgsAr = snapshot.omsorgsAr
                    )
                ),
                OmsorgsgiverUnder70Ar().vilkarsVurder(
                    OmsorgsGiverOgOmsorgsAr(
                        omsorgsgiver = omsorgsyter,
                        omsorgsAr = snapshot.omsorgsAr
                    )
                ),
                omsorgsmottakere.minstEn {
                    FullOmsorgForBarnUnder6().vilkarsVurder(
                        GrunnlagOmsorgForBarnUnder6(
                            omsorgsArbeid = snapshot.omsorgsarbeidPerioder(omsorgsyter, it, prosent = 100),
                            omsorgsmottaker = it,
                            omsorgsAr = snapshot.omsorgsAr
                        )
                    )
                }
            )
        }
    }
}