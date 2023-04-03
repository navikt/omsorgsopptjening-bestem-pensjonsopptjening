package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val person: Person,
    val grunnlag: OmsorgsarbeidSnapshot,
    val omsorgsopptjeningResultater: VilkarsVurdering<*>,
    val utfall: Utfall,
    val omsorgsmottakereInvilget: List<Person> = listOf()
)