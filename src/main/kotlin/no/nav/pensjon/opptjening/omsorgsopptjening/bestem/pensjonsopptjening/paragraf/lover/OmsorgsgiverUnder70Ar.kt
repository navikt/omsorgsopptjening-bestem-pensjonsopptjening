package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.OmsorgsGiverOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon

class OmsorgsgiverUnder70Ar : Vilkar<OmsorgsGiverOgOmsorgsAr>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd til og med det året vedkommende fyller 69 år.",
        begrunnesleForAvslag = "Medlemmet er over 69 år.",
        begrunnelseForInnvilgelse = "Medlemmet er under 70 år.",
    ),
    avgjorelsesFunksjon = `Medlemmet er under 70 ar`,
) {
    companion object {
        private val `Medlemmet er under 70 ar` = fun(input: OmsorgsGiverOgOmsorgsAr) =
            if (input.omsorgsAr - input.omsorgsgiver.fodselsAr!! < 70) {
                Avgjorelse.INVILGET
            } else {
                Avgjorelse.AVSLAG
            }
    }
}