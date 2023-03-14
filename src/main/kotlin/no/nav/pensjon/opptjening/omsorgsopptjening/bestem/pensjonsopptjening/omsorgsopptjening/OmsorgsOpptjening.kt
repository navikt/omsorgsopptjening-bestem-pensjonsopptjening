package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsResultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val person: Person,
    val omsorgsMottakere: List<Person>,
    val grunnlag: OmsorgsarbeidsSnapshot,
    val omsorgsopptjeningResultater: VilkarsResultat<*>,
    val invilget: Avgjorelse
)