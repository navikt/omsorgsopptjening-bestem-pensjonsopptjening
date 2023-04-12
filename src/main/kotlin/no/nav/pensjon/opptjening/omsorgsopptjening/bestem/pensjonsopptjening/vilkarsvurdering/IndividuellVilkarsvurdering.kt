package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.SamletVilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og
import org.springframework.stereotype.Service

@Service
class IndividuellVilkarsvurdering {

    fun vilkarsvurder(snapshot: OmsorgsarbeidSnapshot): SamletVilkarsresultat {
        val omsorgsAr = snapshot.omsorgsAr
        val omsorgsyter = snapshot.omsorgsyter
        val omsorgsmottakere = snapshot.getOmsorgsmottakere(omsorgsyter)

        return SamletVilkarsresultat(
            snapshot = snapshot,
            individueltVilkarsresultat = og(
                OmsorgsgiverOver16Ar().vilkarsVurder(
                    OmsorgsGiverOgOmsorgsAr(
                        omsorgsAr = omsorgsAr,
                        omsorgsgiver = omsorgsyter
                    )
                ),
                OmsorgsgiverUnder70Ar().vilkarsVurder(
                    OmsorgsGiverOgOmsorgsAr(
                        omsorgsAr = omsorgsAr,
                        omsorgsgiver = omsorgsyter
                    )
                ),
                eller(
                    omsorgsmottakere.minstEn {
                        FullOmsorgForBarnUnder6().vilkarsVurder(
                            GrunnlagOmsorgForBarnUnder6(
                                omsorgsAr = omsorgsAr,
                                omsorgsmottaker = it,
                                omsorgsArbeid100Prosent = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                                    omsorgsyter,
                                    it,
                                    prosent = 100
                                ),
                            )
                        )
                    }
                )
            )
        )
    }
}