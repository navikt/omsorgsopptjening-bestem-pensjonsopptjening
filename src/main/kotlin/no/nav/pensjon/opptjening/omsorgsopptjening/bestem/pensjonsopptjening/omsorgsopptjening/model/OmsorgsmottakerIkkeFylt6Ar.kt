package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsmottakerIkkeFylt6Ar : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsmottakerIkkeFylt6ArVurdering {
        return bestemUtfall(grunnlag).let {
            OmsorgsmottakerIkkeFylt6ArVurdering(
                lovhenvisninger = it.lovhenvisning(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        val lovhenvisning = setOf(Lovhenvisning.OMSORGSMOTTAKER_IKKE_FYLT_6_AR)
        return if (grunnlag.alderMottaker(mellom = 0..5)) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
        }
    }
}

data class OmsorgsmottakerIkkeFylt6ArVurdering(
    override val lovhenvisninger: Set<Lovhenvisning>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
