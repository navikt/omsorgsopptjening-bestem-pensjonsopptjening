package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AvslagÅrsak

internal enum class AvslagÅrsakDb {
    MINDRE_ENN_6_MND_FULL_OMSORG,
    BARN_IKKE_MELLOM_1_OG_5,
    OMSORGSYTER_UNDER_16,
    OMSORGSYTER_OVER_69,
    MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT, //ELLER
    ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT, //OG

}

internal fun List<AvslagÅrsak>.toDb(): List<AvslagÅrsakDb> {
    return map { it.toDb() }
}

internal fun AvslagÅrsak.toDb(): AvslagÅrsakDb {
    return when (this) {
        AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG -> AvslagÅrsakDb.MINDRE_ENN_6_MND_FULL_OMSORG
        AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5 -> AvslagÅrsakDb.BARN_IKKE_MELLOM_1_OG_5
        AvslagÅrsak.OMSORGSYTER_IKKE_OVER_16 -> AvslagÅrsakDb.OMSORGSYTER_UNDER_16
        AvslagÅrsak.OMSORGSYTER_OVER_69 -> AvslagÅrsakDb.OMSORGSYTER_OVER_69
        AvslagÅrsak.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsakDb.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT
        AvslagÅrsak.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsakDb.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT
    }
}

internal fun List<AvslagÅrsakDb>.toDomain(): List<AvslagÅrsak> {
    return map { it.toDomain() }
}

internal fun AvslagÅrsakDb.toDomain(): AvslagÅrsak {
    return when (this) {
        AvslagÅrsakDb.MINDRE_ENN_6_MND_FULL_OMSORG -> AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG
        AvslagÅrsakDb.BARN_IKKE_MELLOM_1_OG_5 -> AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5
        AvslagÅrsakDb.OMSORGSYTER_UNDER_16 -> AvslagÅrsak.OMSORGSYTER_IKKE_OVER_16
        AvslagÅrsakDb.OMSORGSYTER_OVER_69 -> AvslagÅrsak.OMSORGSYTER_OVER_69
        AvslagÅrsakDb.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsak.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT
        AvslagÅrsakDb.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsak.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT
    }
}