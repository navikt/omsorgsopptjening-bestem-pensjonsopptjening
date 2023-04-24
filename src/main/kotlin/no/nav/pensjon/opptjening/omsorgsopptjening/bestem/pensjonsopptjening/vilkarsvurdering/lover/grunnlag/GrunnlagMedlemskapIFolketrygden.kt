package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning


data class GrunnlagMedlemskapIFolketrygden(
    val landstilknytning: Landstilknytning,
    val erBosattNorge: Boolean,
    val harLopendePensjonUfore: Boolean

)