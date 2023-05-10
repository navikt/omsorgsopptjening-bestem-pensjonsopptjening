package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.OmsorgsYterOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsInformasjon


class OmsorgsYterOver16Ar : Vilkar<OmsorgsYterOgOmsorgsAr>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Medlemmet er under 17 år.",
        begrunnelseForInnvilgelse = "Medlemmet er over 16 år.",
    ),
    utfallsFunksjon = `Person er over 16 ar`,
) {
    companion object {
        private val `Person er over 16 ar` = fun(input: OmsorgsYterOgOmsorgsAr) =
            if (input.omsorgsAr - input.omsorgsyter.fodselsAr > 16) {
                Utfall.INVILGET
            } else {
                Utfall.AVSLAG
            }
    }
}