package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkarsresultat

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val omsorgsyter: Person,
    val omsorgsGrunnlag: OmsorgsGrunnlag,
    val vilkarsResultat: Vilkarsresultat,
    val utfall: Utfall,
    val omsorgsmottakereInvilget: List<Person> = listOf()
)