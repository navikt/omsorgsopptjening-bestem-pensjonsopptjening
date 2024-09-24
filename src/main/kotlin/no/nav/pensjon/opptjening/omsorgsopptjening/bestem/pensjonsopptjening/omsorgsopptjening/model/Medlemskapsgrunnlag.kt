package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class Medlemskapsgrunnlag(
    val vurderingFraLoveME: LoveMeVurdering,
    val rådata: String,
) {
    enum class LoveMeVurdering {
        MEDLEM_I_FOLKETRYGDEN,
        IKKE_MEDLEM_I_FOLKETRYGDEN,
        UAVKLART_MEDLEMSKAP_I_FOLKETRYGDEN,
    }
}
