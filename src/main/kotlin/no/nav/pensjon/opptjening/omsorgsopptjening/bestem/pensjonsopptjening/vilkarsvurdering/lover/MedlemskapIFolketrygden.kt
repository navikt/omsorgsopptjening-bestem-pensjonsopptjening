package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagMedlemskapIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsInformasjon


class MedlemskapIFolketrygden : Vilkar<GrunnlagMedlemskapIFolketrygden>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Krav om medlemskap i folketrygden",
        begrunnesleForAvslag = "Medlemmet er ikke medlem i folketrygden",
        begrunnelseForInnvilgelse = "Medlemmet er medlem i folketrygden",
    ),
    utfallsFunksjon = `Person er medlem i folketrygden`,
) {
    companion object {
        private val `Person er medlem i folketrygden` = fun(input: GrunnlagMedlemskapIFolketrygden) =
            if (input.landstilknytning == Landstilknytning.NASJONAL) {
                if (input.erBosattNorge)
                    Utfall.INVILGET
                else {
                    Utfall.SAKSBEHANDLING
                }
            } else {
                if(input.harLopendePensjonUfore) {
                    Utfall.SAKSBEHANDLING
                } else {
                    Utfall.INVILGET
                }
            }
    }
}