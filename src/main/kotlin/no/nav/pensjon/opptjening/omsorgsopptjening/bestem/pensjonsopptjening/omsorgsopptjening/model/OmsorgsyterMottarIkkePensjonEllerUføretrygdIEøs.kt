package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori

object OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs :
    ParagrafVilkår<OmsorgsyterMottarIkkePensjonEllerUføretrygdIEøs.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag.omsorgstype()) {
            DomainOmsorgskategori.BARNETRYGD -> emptySet<JuridiskHenvisning>()
            DomainOmsorgskategori.HJELPESTØNAD -> emptySet<JuridiskHenvisning>()
        }.let {
            when {
                grunnlag.erInnvilget() -> {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                }

                grunnlag.erInnvilgetTilTrossForPerioderMedYtelseIEøs() -> {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                }

                grunnlag.manuell() -> {
                    VilkårsvurderingUtfall.Ubestemt.Vilkår(it)
                }

                else -> {
                    VilkårsvurderingUtfall.Avslag.Vilkår(it)
                }
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>() {

        override fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger {
            return Oppgaveopplysninger.Generell(
                oppgavemottaker = behandling.omsorgsyter,
                oppgaveTekst = Oppgave.eøsSakMottarPensjonEllerUføretrygd(behandling.omsorgsmottaker)
            )
        }
    }

    data class Grunnlag private constructor(
        val omsorgsmåneder: Omsorgsmåneder,
        val ytelsemåneder: Ytelsemåneder,
        val landstilknytningmåneder: Landstilknytningmåneder,
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {

        companion object {
            fun new(
                omsorgsmåneder: Omsorgsmåneder,
                ytelsemåneder: Ytelsemåneder,
                landstilknytningmåneder: Landstilknytningmåneder,
                antallMånederRegel: AntallMånederRegel,
            ): Grunnlag {
                return Grunnlag(
                    omsorgsmåneder = if (omsorgsmåneder.erKvalifisertForAutomatiskBehandling(antallMånederRegel)) {
                        omsorgsmåneder.kvalifisererForAutomatiskBehandling()
                    } else {
                        omsorgsmåneder.kvalifisererForManuellBehandling()
                    },
                    ytelsemåneder = ytelsemåneder,
                    landstilknytningmåneder = landstilknytningmåneder,
                    antallMånederRegel = antallMånederRegel
                )
            }

            fun persistent(
                omsorgsmåneder: Omsorgsmåneder,
                ytelsemåneder: Ytelsemåneder,
                landstilknytningmåneder: Landstilknytningmåneder,
                antallMånederRegel: AntallMånederRegel,
            ): Grunnlag {
                return Grunnlag(
                    omsorgsmåneder = omsorgsmåneder,
                    ytelsemåneder = ytelsemåneder,
                    landstilknytningmåneder = landstilknytningmåneder,
                    antallMånederRegel = antallMånederRegel
                )
            }
        }

        fun erInnvilget(): Boolean {
            return ytelsemåneder.alle().intersect(landstilknytningmåneder.alleEøsMåneder()).isEmpty()
        }

        /**
         * Godtar at en person kan ha x antall måneder med ytelse og eøs, så lenge det fortsatt er tilstrekkelig
         * antall omsorgsmåneder utover dette.
         */
        fun erInnvilgetTilTrossForPerioderMedYtelseIEøs(): Boolean {
            require(!erInnvilget()) { "Rekkefølgeavhengig" }
            val ytelseOgEøs = ytelsemåneder.alle().intersect(landstilknytningmåneder.alleEøsMåneder())
            val ikkeYtelseOgEøs = omsorgsmåneder.alle().minus(ytelseOgEøs)
            return ytelseOgEøs.isNotEmpty() && ikkeYtelseOgEøs.count().oppfyller(antallMånederRegel)
        }

        fun manuell(): Boolean {
            require(!erInnvilget() && !erInnvilgetTilTrossForPerioderMedYtelseIEøs()) { "Rekkefølgeavhengig" }
            val ytelseOgEøs = ytelsemåneder.alle().intersect(landstilknytningmåneder.alleEøsMåneder())
            val omsorgOgYtelseEøs =
                omsorgsmåneder.alle().minus(ytelseOgEøs).plus(omsorgsmåneder.alle().intersect(ytelseOgEøs))
            return ytelseOgEøs.isNotEmpty() && omsorgOgYtelseEøs.count().oppfyller(antallMånederRegel)
        }

        fun omsorgstype(): DomainOmsorgskategori {
            return omsorgsmåneder.omsorgstype()
        }

        fun omsorgsmåneder(): Omsorgsmåneder {
            return omsorgsmåneder
        }
    }
}