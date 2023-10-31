package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Landstilknytning
import java.time.YearMonth

object OmsorgsyterMottarBarnetrgyd : ParagrafVilkår<OmsorgsyterMottarBarnetrgyd.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
            påkrevetAntallMåneder = grunnlag.påkrevetAntallMåneder(),
        )
    }

    fun Grunnlag.påkrevetAntallMåneder(): Int {
        return when (this) {
            is Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> 1
            is Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> 1
            is Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> 6
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag) {
            is Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                ).let {
                    if (grunnlag.erOppfylltFor(grunnlag.påkrevetAntallMåneder())) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            is Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
                setOf(
                    Referanse.MåHaMinstHalveÅretMedOmsorgForBarnUnder6,
                ).let {
                    if (grunnlag.erOppfylltFor(grunnlag.påkrevetAntallMåneder())) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            is Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                ).let {
                    if (grunnlag.erOppfylltFor(grunnlag.påkrevetAntallMåneder())) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall,
        val påkrevetAntallMåneder: Int
    ) : ParagrafVurdering<Grunnlag>()


    sealed class Grunnlag : ParagrafGrunnlag() {
        abstract val omsorgsytersUtbetalingsmåneder: Utbetalingsmåneder

        fun erOppfylltFor(påkrevetAntallMåneder: Int): Boolean {
            return omsorgsytersUtbetalingsmåneder.alleMåneder().count() >= påkrevetAntallMåneder
        }

        data class OmsorgsmottakerFødtUtenforOmsorgsår(
            override val omsorgsytersUtbetalingsmåneder: Utbetalingsmåneder,
        ) : Grunnlag()

        data class OmsorgsmottakerFødtIOmsorgsår(
            override val omsorgsytersUtbetalingsmåneder: Utbetalingsmåneder,
        ) : Grunnlag()

        data class OmsorgsmottakerFødtIDesemberOmsorgsår(
            override val omsorgsytersUtbetalingsmåneder: Utbetalingsmåneder,
        ) : Grunnlag()
    }
}


