package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import org.springframework.stereotype.Component

@Component
class IndividuellVilkarsvurdering {

    fun vilkarsvurder(vilkarsresultat: Vilkarsresultat): VilkarsVurdering<*> {
        val snapshot = vilkarsresultat.snapshot
        val omsorgsAr = snapshot.omsorgsAr
        val omsorgsyter = snapshot.omsorgsyter
        val omsorgsmottakere = snapshot.getOmsorgsmottakere(omsorgsyter)

        return omsorgsmottakere.minstEn { omsorgsmottaker ->
            FullOmsorgForBarnUnder6().vilkarsVurder(
                GrunnlagOmsorgForBarnUnder6(
                    omsorgsAr = omsorgsAr,
                    omsorgsmottaker = omsorgsmottaker,
                    utfallPersonVilkarsvurdering = vilkarsresultat.personVilkarsvurdering!!.utfall,
                    omsorgsArbeid100Prosent = snapshot.getOmsorgsarbeidPerioderForRelevanteAr(
                        omsorgsyter,
                        omsorgsmottaker,
                        prosent = 100
                    )
                )
            )
        }
    }
}