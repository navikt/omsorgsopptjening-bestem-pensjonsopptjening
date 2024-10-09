package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori


object OmsorgsyterErMedlemIFolketrygden : ParagrafVilkår<OmsorgsyterErMedlemIFolketrygden.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag.omsorgstype) {
            DomainOmsorgskategori.BARNETRYGD -> {
                setOf(
                    Referanse.MåHaMinstHalveÅretMedOmsorgForBarnUnder6,
                )
            }

            DomainOmsorgskategori.HJELPESTØNAD -> {
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
        val medlemskapsgrunnlag: Medlemskapsgrunnlag,
        val omsorgstype: DomainOmsorgskategori,
    ) : ParagrafGrunnlag() {

        fun erOppfyllt(): Boolean {
            return medlemskapsgrunnlag.unntaksperioder.isEmpty() //TODO
        }
    }
}