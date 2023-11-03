package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

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
        return setOf(
            Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder //TODO rett opp henvinsinger/presiser hvorfor det er slik? tolkning/forskrift/lov whatever
        ).let {
            if (grunnlag.omsorgsyterHarFlestOmsorgsmåneder()) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>() {
        fun hentOppgaveopplysninger(): Oppgaveopplysning {
            return VelgOppgaveForPersonOgInnhold(grunnlag).let {
                if (it.oppgaveForPerson() == grunnlag.omsorgsyter) {
                    Oppgaveopplysning.ToOmsorgsytereMedLikeMangeMånederOmsorg(
                        oppgaveMottaker = it.oppgaveForPerson(),
                        annenOmsorgsyter = it.annenPersonForInnhold(),
                        omsorgsmottaker = it.omsorgsmottaker(),
                        omsorgsår = it.omsorgsår(),
                    )
                } else {
                    Oppgaveopplysning.Ingen
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
        private data class VelgOppgaveForPersonOgInnhold(
            private val grunnlag: Grunnlag,
        ) {
            private val prioritert: List<OmsorgsmånederForMottakerOgÅr> =
                grunnlag
                    .omsorgsytereMedFlestOmsorgsmåneder()
                    .sortedWith(compareBy<OmsorgsmånederForMottakerOgÅr> { it.haddeOmsorgIDesember() }.thenBy { it.omsorgsyter == grunnlag.omsorgsyter })
                    .reversed()

            fun oppgaveForPerson(): String {
                return prioritert.first().omsorgsyter
            }

            fun annenPersonForInnhold(): String {
                return prioritert.first { it.omsorgsyter != grunnlag.omsorgsyter }.omsorgsyter
            }

            fun omsorgsmottaker(): String {
                return prioritert.first().omsorgsmottaker
            }

            fun omsorgsår(): Int {
                return prioritert.first().omsorgsår
            }
        }
    }

    data class Grunnlag(
        val omsorgsyter: String,
        val data: List<OmsorgsmånederForMottakerOgÅr> //TODO stramme opp modellen slik at det ikke er teoretisk mulig med flere mottakere?
    ) : ParagrafGrunnlag() {
        private val omsorgsytereGruppertEtterOmsorgsmåneder = data
            .groupBy { it.antall() }
        private val omsorgsytereMedFlestOmsorgsmåneder = omsorgsytereGruppertEtterOmsorgsmåneder
            .let { map -> map[map.maxOf { it.key }]!! }

        fun omsorgsyterHarFlestOmsorgsmåneder(): Boolean {
            return omsorgsytereMedFlestOmsorgsmåneder().let { omsorgsytere ->
                omsorgsytere.count() == 1 && omsorgsytere.all { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder(): Boolean {
            return omsorgsytereMedFlestOmsorgsmåneder().let { omsorgsytere ->
                omsorgsytere.count() > 1 && omsorgsytere.any { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsytereMedFlestOmsorgsmåneder(): Set<OmsorgsmånederForMottakerOgÅr> {
            return omsorgsytereMedFlestOmsorgsmåneder.toSet()
        }
    }


    data class OmsorgsmånederForMottakerOgÅr(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
        val omsorgsmåneder: GyldigeOmsorgsmåneder,
        val omsorgsår: Int
    ) {
        fun antall(): Int {
            return omsorgsmåneder.alleMåneder().count()
        }

        fun haddeOmsorgIDesember(): Boolean {
            return omsorgsmåneder.alleMåneder().contains(YearMonth.of(omsorgsår, Month.DECEMBER))
        }
    }
}