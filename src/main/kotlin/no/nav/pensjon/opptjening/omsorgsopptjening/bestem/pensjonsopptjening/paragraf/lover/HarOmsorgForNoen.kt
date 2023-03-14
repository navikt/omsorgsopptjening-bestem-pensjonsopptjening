package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person

class HarOmsorgForNoen : Vilkar<List<Person>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "TODO",
        begrunnesleForAvslag = "TODO",
        begrunnelseForInnvilgelse = "TODO",
    ),
    avgjorelsesFunksjon = `Person har omsorg for omsorgsmottaker`,
) {
    companion object {
        private val `Person har omsorg for omsorgsmottaker` = fun(grunnlag: List<Person>) =
            if (grunnlag.isNotEmpty()) {
                Avgjorelse.INVILGET
            } else {
                Avgjorelse.AVSLAG
            }
    }
}