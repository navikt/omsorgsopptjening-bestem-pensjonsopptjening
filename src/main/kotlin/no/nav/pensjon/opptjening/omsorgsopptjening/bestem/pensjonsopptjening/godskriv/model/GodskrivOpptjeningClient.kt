package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype

interface GodskrivOpptjeningClient {
    fun godskriv(
        omsorgsyter: String,
        omsorgsÅr: Int,
        omsorgstype: DomainOmsorgstype,
        kilde: DomainKilde,
        omsorgsmottaker: String,
    )
}