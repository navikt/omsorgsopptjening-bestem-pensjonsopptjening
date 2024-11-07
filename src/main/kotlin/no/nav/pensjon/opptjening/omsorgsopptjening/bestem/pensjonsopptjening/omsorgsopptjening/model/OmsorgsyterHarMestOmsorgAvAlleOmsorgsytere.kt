package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import java.time.Month
import java.time.YearMonth

object OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere :
    ParagrafVilkår<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when {
            grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg() -> {
                VilkårsvurderingUtfall.Innvilget.Vilkår(emptySet()) // Har egentlig ikke noe godt å hekte dette på
            }

            grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg() -> {
                VilkårsvurderingUtfall.Ubestemt(setOf(JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Tredje_Ledd))
            }

            grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg() -> {
                VilkårsvurderingUtfall.Ubestemt(setOf(JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Tredje_Ledd))
            }

            else -> {
                if (grunnlag.omsorgsyterHarFlestOmsorgsmånederUavhengigAvFullEllerDelt()) {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(emptySet()) // Har egentlig ikke noe godt å hekte dette på
                } else {
                    VilkårsvurderingUtfall.Avslag.Vilkår(emptySet()) // Har egentlig ikke noe godt å hekte dette på
                }
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>() {

        override fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger {
            return InnholdsvalgForOppgavetekstHvisFlereOmsorgsytereMedLikeMyeOmsorg(grunnlag).let {
                val oppgavemottaker = it.oppgaveGjelderFnr()
                if (oppgavemottaker == grunnlag.omsorgsyter) {
                    when (behandling.omsorgsmottakerFødtIOmsorgsår()) {
                        true -> {
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = oppgavemottaker,
                                oppgaveTekst = Oppgave.flereOmsorgsytereMedLikeMyeOmsorgFødselsår(omsorgsmottaker = it.omsorgsmottaker())
                            )
                        }

                        false -> {
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = oppgavemottaker,
                                oppgaveTekst = Oppgave.flereOmsorgsytereMedLikeMyeOmsorg(
                                    omsorgsmottaker = it.omsorgsmottaker(),
                                    annenOmsorgsyter = it.annenOmsorgsyterFnr()
                                )

                            )
                        }
                    }
                } else {
                    Oppgaveopplysninger.Ingen
                }
            }
        }

        /**
         * Prioriterer oppgavemottakere etter kriteriene:
         * 1. Flest omsorgsmåneder
         * 2. Hadde omsorg i desember måned
         * 3. Er personen [grunnlag] gjelder for
         *    Dette sørger for at vi alltid prioriterer å sende oppgave for omsorgsyteren behandlingen gjelder.
         */
        private data class InnholdsvalgForOppgavetekstHvisFlereOmsorgsytereMedLikeMyeOmsorg(
            private val grunnlag: Grunnlag,
        ) {
            //TODO ikke veldig pent
            val utvalg = when {
                grunnlag.omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg() -> grunnlag.omsorgsytereMedFlestOmsorgsmånederFull
                grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg() -> grunnlag.omsorgsytereMedFlestOmsorgsmånederFull
                grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg() -> grunnlag.omsorgsytereMedFlestOmsorgsmånederDelt
                else -> throw IllegalStateException("En av alternativene må være oppfylt")
            }

            private val prioritert: List<OmsorgsmånederForMottakerOgÅr> =
                utvalg
                    .sortedWith(compareBy<OmsorgsmånederForMottakerOgÅr> { it.haddeOmsorgIDesember() }.thenBy { it.omsorgsyter == grunnlag.omsorgsyter })
                    .reversed()

            fun oppgaveGjelderFnr(): String {
                return prioritert.first().omsorgsyter
            }

            fun annenOmsorgsyterFnr(): String {
                return prioritert.first { it.omsorgsyter != grunnlag.omsorgsyter }.omsorgsyter
            }

            fun omsorgsmottaker(): String {
                return prioritert.first().omsorgsmottaker
            }
        }
    }

    data class Grunnlag(
        val omsorgsyter: String,
        val data: Set<OmsorgsmånederForMottakerOgÅr> // Det er teoretisk mulig med flere mottakere, men i praksis kun en
    ) : ParagrafGrunnlag() {
        val omsorgsytereGruppertEtterOmsorgsmånederMedFull = data
            .groupBy { it.antallFull() }
            .filter { it.key > 0 }

        val omsorgsytereMedFlestOmsorgsmånederFull = if (omsorgsytereGruppertEtterOmsorgsmånederMedFull.isNotEmpty()) {
            omsorgsytereGruppertEtterOmsorgsmånederMedFull.let { map -> map[map.maxOf { it.key }]!! }.toSet()
        } else {
            emptySet()
        }

        val omsorgsytereGruppertEtterOmsorgsmånederMedDelt = data
            .groupBy { it.antallDelt() }
            .filter { it.key > 0 }

        val omsorgsytereMedFlestOmsorgsmånederDelt = if (omsorgsytereGruppertEtterOmsorgsmånederMedDelt.isNotEmpty()) {
            omsorgsytereGruppertEtterOmsorgsmånederMedDelt
                .let { map -> map[map.maxOf { it.key }]!! }
                .toSet()
        } else {
            emptySet()
        }

        val omsorgsytereGruppertEtterOmsorgsmånederMedFullEllerDelt = data
            .groupBy { it.antall() }
            .filter { it.key > 0 }

        val omsorgsytereMedFlestOmsorgsmånederFullEllerDelt =
            if (omsorgsytereGruppertEtterOmsorgsmånederMedFullEllerDelt.isNotEmpty()) {
                omsorgsytereGruppertEtterOmsorgsmånederMedFullEllerDelt.let { map -> map[map.maxOf { it.key }]!! }
                    .toSet()
            } else {
                emptySet()
            }

        fun omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg(): Boolean {
            return omsorgsytereMedFlestOmsorgsmånederFull.let { omsorgsytere ->
                omsorgsytere.count() == 1 && omsorgsytere.all { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg(): Boolean {
            require(!omsorgsyterHarFlestOmsorgsmånederMedFullOmsorg()) { "Rekkefølgeavhengig" }
            return omsorgsytereMedFlestOmsorgsmånederFull.isNotEmpty() && omsorgsytereMedFlestOmsorgsmånederFull.let { omsorgsytere ->
                omsorgsytere.count() > 1 && omsorgsytere.any { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedDeltOmsorg(): Boolean {
            require(!omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()) { "Rekkefølgeavhengig" }
            return omsorgsytereGruppertEtterOmsorgsmånederMedDelt.isNotEmpty() && omsorgsytereMedFlestOmsorgsmånederDelt.let { omsorgsytere ->
                omsorgsytere.count() > 1 && omsorgsytere.any { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsyterHarFlestOmsorgsmånederUavhengigAvFullEllerDelt(): Boolean {
            require(!omsorgsyterErEnAvFlereMedFlestOmsorgsmånederMedFullOmsorg()) { "Rekkefølgeavhengig" }
            return omsorgsytereMedFlestOmsorgsmånederFullEllerDelt.isNotEmpty() && omsorgsytereMedFlestOmsorgsmånederFullEllerDelt.let { omsorgsytere ->
                omsorgsytere.count() == 1 && omsorgsytere.any { it.omsorgsyter == omsorgsyter }
            }
        }
    }


    data class OmsorgsmånederForMottakerOgÅr(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
        val omsorgsmåneder: Omsorgsmåneder,
        val omsorgsår: Int
    ) {
        fun antallFull(): Int {
            return when (omsorgsmåneder) {
                is Omsorgsmåneder.Barnetrygd -> omsorgsmåneder.antallFull()
                is Omsorgsmåneder.Hjelpestønad -> omsorgsmåneder.antall() //TODO hull for hjelpestønad med delt barnetrygd
            }
        }

        fun haddeOmsorgIDesember(): Boolean {
            return omsorgsmåneder.alle().contains(YearMonth.of(omsorgsår, Month.DECEMBER))
        }

        fun antallDelt(): Int {
            return when (omsorgsmåneder) {
                is Omsorgsmåneder.Barnetrygd -> omsorgsmåneder.antallDelt()
                is Omsorgsmåneder.Hjelpestønad -> omsorgsmåneder.antall() //TODO hull for hjelpestønad med delt barnetrygd
            }
        }

        fun antall(): Int {
            return omsorgsmåneder.antall()
        }
    }
}