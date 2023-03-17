package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon


class OmsorgsgiverOver16Ar : Vilkar<OmsorgsGiverOgOmsorgsAr>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Medlemmet er under 17 år.",
        begrunnelseForInnvilgelse = "Medlemmet er over 16 år.",
    ),
    utfallsFunksjon = `Person er over 16 ar`,
) {
    companion object {
        private val `Person er over 16 ar` = fun(input: OmsorgsGiverOgOmsorgsAr) =
            if (input.omsorgsAr - input.omsorgsgiver.fodselsAr!! > 16) {
                Utfall.INVILGET
            } else {
                Utfall.AVSLAG
            }
    }
}