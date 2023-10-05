package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype

interface GodskrivOpptjeningClient {
    fun godskriv(
        omsorgsyter: String,
        omsorgsÅr: Int,
        omsorgstype: DomainOmsorgstype,
        kilde: DomainKilde,
        omsorgsmottaker: String,
    )
}