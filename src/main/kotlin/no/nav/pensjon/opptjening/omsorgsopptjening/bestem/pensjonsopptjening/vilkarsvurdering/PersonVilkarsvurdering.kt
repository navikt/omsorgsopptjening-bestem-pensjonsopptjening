package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsYterOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsyterUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsYterOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og
import org.springframework.stereotype.Component


//TODO add tests
@Component
class PersonVilkarsvurdering {
    fun vilkarsvurder(vilkarsResultat: Vilkarsresultat): VilkarsVurdering<List<VilkarsVurdering<*>>> {
        val omsorgsAr = vilkarsResultat.snapshot.omsorgsAr
        val omsorgsyter = vilkarsResultat.snapshot.omsorgsyter

        return og(
            OmsorgsYterOver16Ar().vilkarsVurder(
                OmsorgsYterOgOmsorgsAr(
                    omsorgsAr = omsorgsAr,
                    omsorgsyter = omsorgsyter
                )
            ),
            OmsorgsyterUnder70Ar().vilkarsVurder(
                OmsorgsYterOgOmsorgsAr(
                    omsorgsAr = omsorgsAr,
                    omsorgsyter = omsorgsyter
                )
            )
        )
    }
}