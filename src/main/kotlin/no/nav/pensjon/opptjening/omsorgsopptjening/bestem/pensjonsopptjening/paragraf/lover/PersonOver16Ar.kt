package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar


class PersonOver16Ar : Vilkar<PersonOgOmsorgsAr>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Medlemmet er under 17 år.",
        begrunnelseForInnvilgelse = "Medlemmet er over 16 år.",
    ),
    kalkulerAvgjorelse = `Person er over 16 ar`,
) {
    companion object {
        private val `Person er over 16 ar` = fun(input: PersonOgOmsorgsAr) = input.omsorgsAr - input.person.fodselsAr!! > 16
    }
}