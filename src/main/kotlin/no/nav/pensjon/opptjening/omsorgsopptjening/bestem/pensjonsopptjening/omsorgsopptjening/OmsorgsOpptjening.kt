package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val person: Person,
    val grunnlag: OmsorgsarbeidsSnapshot,
    val omsorgsopptjeningResultater: VilkarsVurdering<*>,
    val utfall: Utfall,
    val omsorgsmottakereInvilget: List<Person> = listOf()
)