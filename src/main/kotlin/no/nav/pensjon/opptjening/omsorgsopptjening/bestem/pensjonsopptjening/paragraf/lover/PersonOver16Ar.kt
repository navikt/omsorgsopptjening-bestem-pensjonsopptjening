package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon


class PersonOver16Ar : Vilkar<PersonOgOmsorgsAr>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Medlemmet er under 17 år.",
        begrunnelseForInnvilgelse = "Medlemmet er over 16 år.",
    ),
    avgjorelsesFunksjon = `Person er over 16 ar`,
) {
    companion object {
        private val `Person er over 16 ar` = fun(input: PersonOgOmsorgsAr) =
            if (input.omsorgsAr - input.person.fodselsAr!! > 16) {
                Avgjorelse.INVILGET
            } else {
                Avgjorelse.AVSLAG
            }
    }
}