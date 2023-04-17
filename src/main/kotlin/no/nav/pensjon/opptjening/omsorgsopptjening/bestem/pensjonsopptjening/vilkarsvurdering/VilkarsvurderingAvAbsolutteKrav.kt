package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsgiverUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og
import org.springframework.stereotype.Component

@Component
class VilkarsvurderingAvAbsolutteKrav {
    fun vilkarsvurder(vilkarsResultat: Vilkarsresultat): VilkarsVurdering<List<VilkarsVurdering<*>>> {
        val snapshot = vilkarsResultat.snapshot
        val omsorgsAr = snapshot.omsorgsAr
        val omsorgsyter = snapshot.omsorgsyter

        return og(
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
            )
        )
    }
}