package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori

interface GodskrivOpptjeningClient {
    fun godskriv(
        omsorgsyter: String,
        omsorgsÅr: Int,
        omsorgstype: DomainOmsorgskategori,
        omsorgsmottaker: String,
    )
}