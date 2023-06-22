package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår

/**
 * Når to omsorgsytere har tilnærmet lik antall måneder omsorg for en omrogsmottaker
 * må omsorgsyterne sette fram krav om omsorgsopptjening med opplysning om hvem av
 * omsorgsyterne som skal ha opptjeningen for kalenderåret
 */
class LiktAntallMånederOmsorg : ParagrafVilkår<LiktAntallMånederOmsorgGrunnlag>() {
    override fun vilkarsVurder(grunnlag: LiktAntallMånederOmsorgGrunnlag): LiktAntallMånederOmsorgVurdering {
        return bestemUtfall(grunnlag).let {
            LiktAntallMånederOmsorgVurdering(
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<LiktAntallMånederOmsorgGrunnlag>> T.bestemUtfall(grunnlag: LiktAntallMånederOmsorgGrunnlag): VilkårsvurderingUtfall {
        return setOf(Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder()).let {
            if (grunnlag.finnesAndreOmsorgsytereMedLikeMangeManeder()) {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(setOf(Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder()))
            } else {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(setOf(Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder()))
            }
        }
    }
}

data class LiktAntallMånederOmsorgVurdering(
    override val henvisninger: Set<Henvisning>,
    override val grunnlag: LiktAntallMånederOmsorgGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<LiktAntallMånederOmsorgGrunnlag>()

data class LiktAntallMånederOmsorgGrunnlag(
    val omsorgsyter: YterMottakerManeder,
    val andreOmsorgsytere: List<YterMottakerManeder>
) : ParagrafGrunnlag() {
    init {
        require(andreOmsorgsytere.none { it.omsorgsyter == omsorgsyter.omsorgsyter }) { "Omsorgsyter som behandles kan ikke være i listen av andre omsorgsytere" }
        require(if(andreOmsorgsytere.isNotEmpty()) andreOmsorgsytere.map { it.omsorgsmottaker }.distinct().single() == omsorgsyter.omsorgsmottaker else true)
    }

    fun finnesAndreOmsorgsytereMedLikeMangeManeder(): Boolean {
        return andreOmsorgsytere.any { omsorgsyter.antallManeder == it.antallManeder }
    }
}

data class YterMottakerManeder(
    val omsorgsyter: PersonMedFødselsår,
    val omsorgsmottaker: PersonMedFødselsår,
    val antallManeder: Int,
)

