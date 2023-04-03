package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

data class OmsorgasbeidsInformasjon(
    val omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot,
    val relaterteOmsorgsarbeidSnapshot: List<OmsorgsarbeidSnapshot> = listOf()
)