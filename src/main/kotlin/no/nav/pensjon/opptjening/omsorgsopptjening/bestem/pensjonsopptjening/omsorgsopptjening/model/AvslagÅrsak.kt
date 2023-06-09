package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

enum class AvslagÅrsak {
    MINDRE_ENN_6_MND_FULL_OMSORG,
    BARN_IKKE_MELLOM_1_OG_5,
    OMSORGSYTER_IKKE_OVER_16,
    OMSORGSYTER_OVER_69,
    MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT, //ELLER
    ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT, //OG
    ALDER_IKKE_0,
    INGEN_MÅNEDER_FULL_OMSORG,
    INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL,
}