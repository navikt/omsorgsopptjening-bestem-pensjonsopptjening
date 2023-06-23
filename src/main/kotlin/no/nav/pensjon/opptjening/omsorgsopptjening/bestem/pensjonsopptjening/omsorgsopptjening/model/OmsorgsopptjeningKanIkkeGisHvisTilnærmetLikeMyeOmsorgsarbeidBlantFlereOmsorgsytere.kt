package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

/**
 * Når to omsorgsytere har tilnærmet lik antall måneder omsorg for en omrogsmottaker
 * må omsorgsyterne sette fram krav om omsorgsopptjening med opplysning om hvem av
 * omsorgsyterne som skal ha opptjeningen for kalenderåret
 */
object OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere :
    ParagrafVilkår<OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder
        ).let {
            if (grunnlag.finnesAndreOmsorgsytereMedLikeMangeManeder()) {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsyter: YterMottakerManeder,
        val andreOmsorgsytere: List<YterMottakerManeder>
    ) : ParagrafGrunnlag() {
        init {
            require(andreOmsorgsytere.none { it.omsorgsyter == omsorgsyter.omsorgsyter }) { "Omsorgsyter som behandles kan ikke være i listen av andre omsorgsytere" }
            require(
                if (andreOmsorgsytere.isNotEmpty()) andreOmsorgsytere.map { it.omsorgsmottaker }.distinct()
                    .single() == omsorgsyter.omsorgsmottaker else true
            )
        }

        fun finnesAndreOmsorgsytereMedLikeMangeManeder(): Boolean {
            return andreOmsorgsytere.any { omsorgsyter.antallManeder == it.antallManeder }
        }

        data class YterMottakerManeder(
            val omsorgsyter: PersonMedFødselsår,
            val omsorgsmottaker: PersonMedFødselsår,
            val antallManeder: Int,
        )
    }
}
