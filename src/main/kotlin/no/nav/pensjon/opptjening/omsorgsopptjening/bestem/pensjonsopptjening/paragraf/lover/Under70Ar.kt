package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.PersonOgOmsorgsAr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.RegelInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar

class Under70Ar : Vilkar<PersonOgOmsorgsAr>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd til og med det året vedkommende fyller 69 år.",
        begrunnesleForAvslag = "Medlemmet er over 69 år.",
        begrunnelseForInnvilgelse = "Medlemmet er under 70 år.",
    ),
    oppfyllerRegler = `Medlemmet er under 70 år`,
) {
    companion object {
        val `Medlemmet er under 70 år` = fun(input: PersonOgOmsorgsAr) = input.omsorgsAr - input.person.fodselsAr!! < 70
    }
}