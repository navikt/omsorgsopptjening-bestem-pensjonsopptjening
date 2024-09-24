package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype


object OmsorgsyterErMedlemIFolketrygden : ParagrafVilkår<OmsorgsyterErMedlemIFolketrygden.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag.omsorgstype) {
            DomainOmsorgstype.BARNETRYGD -> {
                setOf(
                    Referanse.MåHaMinstHalveÅretMedOmsorgForBarnUnder6,
                )
            }

            DomainOmsorgstype.HJELPESTØNAD -> {
                setOf(
                    Referanse.MåHaMinstHalveÅretMedOmsorgForSykFunksjonshemmetEllerEldre,
                )
            }
        }.let {
            if (grunnlag.erOppfyllt()) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }


    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val loveMEVurdering: Medlemskapsgrunnlag.LoveMeVurdering,
        val omsorgstype: DomainOmsorgstype,
    ) : ParagrafGrunnlag() {

        fun erOppfyllt(): Boolean {
            return loveMEVurdering != Medlemskapsgrunnlag.LoveMeVurdering.IKKE_MEDLEM_I_FOLKETRYGDEN
        }
    }
}