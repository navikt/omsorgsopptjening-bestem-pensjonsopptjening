package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AvslagÅrsak

internal enum class AvslagÅrsakDb {
    MINDRE_ENN_6_MND_FULL_OMSORG,
    BARN_IKKE_MELLOM_1_OG_5,
    OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR,
    OMSORGSYTER_FYLT_70_VED_UTGANG_AV_OMSORGSÅR,
    MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT, //ELLER
    ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT, //OG
    ALDER_IKKE_0,
    INGEN_MÅNEDER_FULL_OMSORG,
    INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL,
    ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER,
    ALLEREDE_GODSKREVET_BARN_FOR_ÅR,
}

internal fun List<AvslagÅrsak>.toDb(): List<AvslagÅrsakDb> {
    return map { it.toDb() }
}

internal fun AvslagÅrsak.toDb(): AvslagÅrsakDb {
    return when (this) {
        AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG -> AvslagÅrsakDb.MINDRE_ENN_6_MND_FULL_OMSORG
        AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5 -> AvslagÅrsakDb.BARN_IKKE_MELLOM_1_OG_5
        AvslagÅrsak.OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR -> AvslagÅrsakDb.OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR
        AvslagÅrsak.OMSORGSYTER_ELDRE_ENN_69_VED_UTGANG_AV_OMSORGSÅR -> AvslagÅrsakDb.OMSORGSYTER_FYLT_70_VED_UTGANG_AV_OMSORGSÅR
        AvslagÅrsak.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsakDb.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT
        AvslagÅrsak.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsakDb.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT
        AvslagÅrsak.ALDER_IKKE_0 -> AvslagÅrsakDb.ALDER_IKKE_0
        AvslagÅrsak.INGEN_MÅNEDER_FULL_OMSORG -> AvslagÅrsakDb.INGEN_MÅNEDER_FULL_OMSORG
        AvslagÅrsak.INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL -> AvslagÅrsakDb.INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL
        AvslagÅrsak.ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER -> AvslagÅrsakDb.ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER
        AvslagÅrsak.ALLEREDE_GODSKREVET_BARN_FOR_ÅR -> AvslagÅrsakDb.ALLEREDE_GODSKREVET_BARN_FOR_ÅR
    }
}

internal fun List<AvslagÅrsakDb>.toDomain(): List<AvslagÅrsak> {
    return map { it.toDomain() }
}

internal fun AvslagÅrsakDb.toDomain(): AvslagÅrsak {
    return when (this) {
        AvslagÅrsakDb.MINDRE_ENN_6_MND_FULL_OMSORG -> AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG
        AvslagÅrsakDb.BARN_IKKE_MELLOM_1_OG_5 -> AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5
        AvslagÅrsakDb.OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR -> AvslagÅrsak.OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR
        AvslagÅrsakDb.OMSORGSYTER_FYLT_70_VED_UTGANG_AV_OMSORGSÅR -> AvslagÅrsak.OMSORGSYTER_ELDRE_ENN_69_VED_UTGANG_AV_OMSORGSÅR
        AvslagÅrsakDb.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsak.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT
        AvslagÅrsakDb.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT -> AvslagÅrsak.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT
        AvslagÅrsakDb.ALDER_IKKE_0 -> AvslagÅrsak.ALDER_IKKE_0
        AvslagÅrsakDb.INGEN_MÅNEDER_FULL_OMSORG -> AvslagÅrsak.INGEN_MÅNEDER_FULL_OMSORG
        AvslagÅrsakDb.INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL -> AvslagÅrsak.INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL
        AvslagÅrsakDb.ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER -> AvslagÅrsak.ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER
        AvslagÅrsakDb.ALLEREDE_GODSKREVET_BARN_FOR_ÅR -> AvslagÅrsak.ALLEREDE_GODSKREVET_BARN_FOR_ÅR
    }
}