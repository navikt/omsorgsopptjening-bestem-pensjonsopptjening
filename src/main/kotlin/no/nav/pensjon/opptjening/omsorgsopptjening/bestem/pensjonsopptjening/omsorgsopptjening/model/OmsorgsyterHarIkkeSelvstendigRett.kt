package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori


object OmsorgsyterHarIkkeSelvstendigRett : ParagrafVilkår<OmsorgsyterHarIkkeSelvstendigRett.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when(grunnlag.erOppfyllt()){
            true -> VilkårsvurderingUtfall.Innvilget.Vilkår(emptySet())
            false -> VilkårsvurderingUtfall.Avslag.Vilkår(emptySet())
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    @ConsistentCopyVisibility
    data class Grunnlag private constructor(
        val selvstendigRettMåneder: SelvstendigRettMåneder,
        val omsorgsmåneder: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {

        companion object {
            fun new(
                selvstendigRettMåneder: SelvstendigRettMåneder,
                omsorgsmåneder: Omsorgsmåneder,
                antallMånederRegel: AntallMånederRegel,
            ): Grunnlag {
                return Grunnlag(
                    selvstendigRettMåneder = selvstendigRettMåneder,
                    omsorgsmåneder = if (omsorgsmåneder.erKvalifisertForAutomatiskBehandling(antallMånederRegel)) {
                        omsorgsmåneder.kvalifisererForAutomatiskBehandling()
                    } else {
                        omsorgsmåneder.kvalifisererForManuellBehandling()
                    },
                    antallMånederRegel = antallMånederRegel
                )
            }

            fun persistent(
                selvstendigRettMåneder: SelvstendigRettMåneder,
                omsorgsmåneder: Omsorgsmåneder,
                antallMånederRegel: AntallMånederRegel,
            ): Grunnlag {
                return Grunnlag(
                    selvstendigRettMåneder = selvstendigRettMåneder,
                    omsorgsmåneder = omsorgsmåneder,
                    antallMånederRegel = antallMånederRegel
                )
            }
        }

        fun erOppfyllt(): Boolean {
            //TODO ulike vilkår påvirker hverandre.. trenger vi en "ikke vurdert ting"?
            //TODO gir ikke mening å vurdere dette dersom omsorgsmåneder er ikke oppfyller alene
            return omsorgsmåneder.alle().minus(selvstendigRettMåneder.alle()).oppfyller(antallMånederRegel)
        }

        fun omsorgstype(): DomainOmsorgskategori {
            return omsorgsmåneder.omsorgstype()
        }

        fun omsorgsmåneder(): Omsorgsmåneder {
            return omsorgsmåneder
        }
    }
}