package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.minstEn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og
import org.springframework.stereotype.Service

@Service
class VilkarsvurderingService {

    fun vilkarsvurder(
        snapshot: OmsorgsarbeidSnapshot,
        relaterteSnapshot: List<OmsorgsarbeidSnapshot> = listOf()
    ): VilkarsVurdering<*> {

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