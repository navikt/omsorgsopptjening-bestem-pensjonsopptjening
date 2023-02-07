package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar.VilkarsResultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsOpptjening private constructor(
    val omsorgsAr: Int,
    val person: Person,
    val grunnlag: OmsorgsArbeidSak, //TODO lag grunnlag som er statisk for denne omsorgsopptjeningen
    val omsorgsopptjeningResultater: VilkarsResultat<*>,
    val invilget: Boolean
) {
    fun harOmsorgsOpptjening() = invilget

    companion object {
        fun lagOmsorgsopptjening(
            omsorgsAr: Int,
            person: Person,
            grunnlag: OmsorgsArbeidSak,
            omsorgsopptjeningResultater: VilkarsResultat<*>,
        ) = OmsorgsOpptjening(
            omsorgsAr,
            person,
            grunnlag,
            omsorgsopptjeningResultater,
            omsorgsopptjeningResultater.oppFyllerRegel
        )
    }
}